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

const allFormatOptions = [
  { label: 'PDF', value: 'pdf' },
  { label: 'Word (docx)', value: 'docx' },
  { label: 'Excel (xlsx)', value: 'xlsx' },
  { label: 'HTML', value: 'html' },
  { label: 'Markdown', value: 'md' },
  { label: 'JSON', value: 'json' },
  { label: 'YAML', value: 'yaml' },
  { label: 'CSV', value: 'csv' },
  { label: '图片(JPG)', value: 'jpg' },
  { label: '图片(PNG)', value: 'png' },
];

// 源格式到目标格式的映射
const formatMap: Record<string, string[]> = {
  pdf:    ['docx'],
  docx:   ['pdf', 'html'],
  txt:    ['docx', 'pdf'],
  xlsx:   ['json'],
  md:     ['html'],
  html:   ['pdf'],
  csv:    ['json'],
  json:   ['csv', 'yaml'],
  yaml:   ['json'],
  // 可扩展图片互转
};

const textFormats = ['json', 'html', 'md', 'yaml', 'txt', 'csv'];

const Convert = () => {
  const [file, setFile] = useState<File | null>(null);
  const [targetFormat, setTargetFormat] = useState<string>('');
  const [sourceFormat, setSourceFormat] = useState<string>('');
  const [converting, setConverting] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [previewContent, setPreviewContent] = useState<string | null>(null);
  const [previewType, setPreviewType] = useState<string>('');

  useEffect(() => {
    if (file) {
      const ext = file.name.split('.').pop()?.toLowerCase() || '';
      setSourceFormat(ext);
      // 自动过滤目标格式
      setTargetFormat('');
    }
  }, [file]);

  const handleUpload = (info: any) => {
    try {
      const { file: uploadFile } = info;
      if (uploadFile.size > 100 * 1024 * 1024) {
        message.error('文件大小不能超过100MB');
        return;
      }
      const fileObj = uploadFile.originFileObj || uploadFile;
      setFile(fileObj);
      setError(null);
      setPreviewContent(null);
      setPreviewType('');
      message.success(`${fileObj.name} 文件已选择`);
    } catch (err) {
      setError('文件上传失败');
      message.error('文件上传失败，请重试');
    }
  };

  const handleConvert = async () => {
    if (!file || !targetFormat) {
      message.error('请选择文件和目标格式');
      return;
    }
    setConverting(true);
    setProgress(0);
    setError(null);
    setPreviewContent(null);
    setPreviewType('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('targetFormat', targetFormat);
      formData.append('sourceFormat', sourceFormat);
      const response = await api.post('/convert', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 100)
          );
          setProgress(percentCompleted);
        },
      });
      // 文本格式直接预览
      if (typeof response.data === 'string' || textFormats.includes(targetFormat)) {
        setPreviewContent(response.data);
        setPreviewType(targetFormat);
        message.success('转换成功，已在下方预览');
      } else if (response.data.url) {
        // 二进制格式自动下载
        const link = document.createElement('a');
        link.href = response.data.url;
        link.download = `converted.${targetFormat}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        message.success('转换成功，已自动下载');
      } else {
        throw new Error('转换失败：服务器未返回下载链接');
      }
    } catch (error) {
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

  // 动态过滤目标格式（只展示支持的目标格式）
  const filteredFormatOptions = sourceFormat && formatMap[sourceFormat]
    ? allFormatOptions.filter(opt => formatMap[sourceFormat].includes(opt.value))
    : [];

  // 预览区渲染
  const renderPreview = () => {
    if (!previewContent) return null;
    if (previewType === 'json' || previewType === 'yaml' || previewType === 'csv' || previewType === 'txt' || previewType === 'md') {
      return (
        <pre style={{ background: '#f6f8fa', padding: 16, maxHeight: 400, overflow: 'auto' }}>{previewContent}</pre>
      );
    }
    if (previewType === 'html') {
      return (
        <div style={{ background: '#fff', padding: 16, maxHeight: 400, overflow: 'auto', border: '1px solid #eee' }}
          dangerouslySetInnerHTML={{ __html: previewContent }} />
      );
    }
    return null;
  };

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">文件格式转换</h1>
      <StyledDragger
        name="file"
        multiple={false}
        onChange={handleUpload}
        beforeUpload={() => false}
        accept={allFormatOptions.map(opt => '.' + opt.value).join(',')}
        showUploadList={true}
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此处</p>
        <p className="ant-upload-hint">
          支持PDF、Word、Excel、HTML、Markdown、JSON、YAML、CSV、图片等格式，文件大小不超过100MB
        </p>
      </StyledDragger>
      <OptionsWrapper>
        <Select
          style={{ width: 200 }}
          placeholder={sourceFormat && filteredFormatOptions.length === 0 ? '该格式暂不支持转换' : '选择目标格式'}
          onChange={handleFormatChange}
          value={targetFormat}
          options={filteredFormatOptions}
          disabled={filteredFormatOptions.length === 0}
        />
        <Button
          type="primary"
          onClick={handleConvert}
          loading={converting}
          disabled={!file || !targetFormat || filteredFormatOptions.length === 0}
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
        {sourceFormat && <p>源格式: {sourceFormat}</p>}
        {targetFormat && <p>目标格式: {targetFormat}</p>}
        {error && <p style={{ color: '#ff4d4f' }}>错误信息: {error}</p>}
        {converting && <p>正在转换中...</p>}
      </div>
      {/* 预览区 */}
      {renderPreview()}
    </Container>
  );
};

export default Convert; 