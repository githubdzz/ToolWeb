import { useState, useEffect } from 'react';
import { Upload, Select, Button, Progress, message } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import styled from 'styled-components';
import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

// 添加请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config);
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 添加响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response);
    return response;
  },
  (error) => {
    console.error('响应错误:', error);
    const errorMessage = error.response?.data?.message || error.message || '未知错误';
    message.error(`转换失败: ${errorMessage}`);
    return Promise.reject(error);
  }
);

const { Dragger } = Upload;

const Container = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
`;

const StyledDragger = styled(Dragger)`
  margin-bottom: 24px;
`;

const OptionsWrapper = styled.div`
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
`;

const Convert = () => {
  const [file, setFile] = useState<File | null>(null);
  const [targetFormat, setTargetFormat] = useState<string>('');
  const [converting, setConverting] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    console.log('文件状态:', file?.name);
    console.log('目标格式:', targetFormat);
  }, [file, targetFormat]);

  const handleUpload = (info: any) => {
    console.log('【前端】文件选择:', info.file);
    try {
      const { file: uploadFile } = info;
      console.log('上传文件信息:', uploadFile);
      
      if (uploadFile.size > 100 * 1024 * 1024) {
        message.error('文件大小不能超过100MB');
        return;
      }

      const fileObj = uploadFile.originFileObj || uploadFile;
      setFile(fileObj);
      setError(null);
      message.success(`${fileObj.name} 文件已选择`);
      
      if (!targetFormat) {
        const sourceFormat = fileObj.name.split('.').pop()?.toLowerCase() || '';
        const availableFormats = formatOptions.map(opt => opt.value);
        const suggestedFormat = availableFormats.find(fmt => fmt !== sourceFormat);
        if (suggestedFormat) {
          setTargetFormat(suggestedFormat);
          message.info(`已自动选择转换格式：${suggestedFormat}`);
        }
      }
    } catch (err) {
      console.error('文件上传错误:', err);
      setError('文件上传失败');
      message.error('文件上传失败，请重试');
    }
  };

  const handleConvert = async () => {
    console.log('【前端】开始转换，文件:', file, '目标格式:', targetFormat);
    if (!file || !targetFormat) {
      message.error('请选择文件和目标格式');
      return;
    }

    setConverting(true);
    setProgress(0);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('targetFormat', targetFormat);

      console.log('开始发送转换请求');
      const response = await api.post('/convert', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 100)
          );
          console.log('【前端】上传进度:', percentCompleted);
          setProgress(percentCompleted);
        },
      });

      console.log('【前端】后端响应:', response.data);

      if (response.data.url) {
        const link = document.createElement('a');
        link.href = response.data.url;
        link.download = `converted.${targetFormat}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        message.success('转换成功！');
      } else {
        throw new Error('转换失败：服务器未返回下载链接');
      }
    } catch (error) {
      console.error('【前端】转换失败:', error);
      const errorMessage = error instanceof Error ? error.message : '未知错误';
      setError(errorMessage);
      message.error('转换失败：' + errorMessage);
    } finally {
      setConverting(false);
      setProgress(0);
    }
  };

  const handleFormatChange = (value: string) => {
    console.log('【前端】目标格式选择:', value);
    setTargetFormat(value);
    setError(null);
  };

  const formatOptions = [
    { label: 'PDF', value: 'pdf' },
    { label: 'Word', value: 'docx' },
    { label: 'Excel', value: 'xlsx' },
    { label: 'JPG', value: 'jpg' },
    { label: 'PNG', value: 'png' },
  ];

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">文件格式转换</h1>
      
      <StyledDragger
        name="file"
        multiple={false}
        onChange={handleUpload}
        beforeUpload={() => false}
        accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png"
        showUploadList={true}
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此处</p>
        <p className="ant-upload-hint">
          支持PDF、Word、Excel、图片等格式，文件大小不超过100MB
        </p>
      </StyledDragger>

      <OptionsWrapper>
        <Select
          style={{ width: 200 }}
          placeholder="选择目标格式"
          onChange={handleFormatChange}
          value={targetFormat}
          options={formatOptions}
        />
        <Button
          type="primary"
          onClick={handleConvert}
          loading={converting}
          disabled={!file || !targetFormat}
        >
          开始转换
        </Button>
      </OptionsWrapper>

      {converting && (
        <Progress
          percent={progress}
          status={progress === 100 ? 'success' : 'active'}
        />
      )}

      {/* 状态和错误信息显示 */}
      <div style={{ marginTop: '20px', color: '#666' }}>
        {file && <p>已选择文件: {file.name}</p>}
        {targetFormat && <p>目标格式: {targetFormat}</p>}
        {error && <p style={{ color: '#ff4d4f' }}>错误信息: {error}</p>}
        {converting && <p>正在转换中...</p>}
      </div>
    </Container>
  );
};

export default Convert; 