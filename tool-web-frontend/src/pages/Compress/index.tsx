import { useState } from 'react';
import { Upload, Radio, Input, Button, Progress, message, Tabs, Modal } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import styled from 'styled-components';

const { Dragger } = Upload;
const { Password } = Input;

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
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
`;

const Compress = () => {
  // 压缩Tab相关state
  const [compressFiles, setCompressFiles] = useState<File[]>([]);
  const [compressFileList, setCompressFileList] = useState<any[]>([]);
  const [format, setFormat] = useState('zip');
  const [compressPassword, setCompressPassword] = useState('');
  // 解压Tab相关state
  const [decompressFile, setDecompressFile] = useState<File | null>(null);
  const [decompressFileList, setDecompressFileList] = useState<any[]>([]);
  const [decompressPassword, setDecompressPassword] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);

  // 压缩Tab上传
  const handleCompressUpload = (info: any) => {
    setCompressFileList(info.fileList);
    const newFiles = info.fileList.map((file: any) => file.originFileObj || file);
    setCompressFiles(newFiles);
    message.success(`${newFiles.length} 个文件已选择`);
  };

  // 解压Tab上传
  const handleDecompressUpload = (info: any) => {
    setDecompressFileList(info.fileList);
    const file = info.fileList[0]?.originFileObj || info.fileList[0];
    setDecompressFile(file || null);
    if (file) {
      message.success(`${file.name} 已选择`);
    }
  };

  const handleCompress = async () => {
    if (compressFiles.length === 0) {
      message.error('请选择要压缩的文件');
      return;
    }
    setProcessing(true);
    setProgress(0);
    try {
      const formData = new FormData();
      compressFiles.forEach((file) => formData.append('files', file));
      formData.append('format', format);
      if (compressPassword) formData.append('password', compressPassword);
      const response = await fetch('/api/compress', {
        method: 'POST',
        body: formData,
      });
      const data = await response.json();
      if (data.url) {
        const link = document.createElement('a');
        link.href = data.url;
        link.download = '';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        message.success('压缩完成，已自动下载');
      } else {
        message.success(data.message || '压缩完成');
      }
    } catch (e) {
      message.error('压缩失败');
    } finally {
      setProcessing(false);
      setProgress(0);
    }
  };

  const handleDecompress = async () => {
    if (!decompressFile) {
      message.error('请选择要解压的文件');
      return;
    }
    setProcessing(true);
    setProgress(0);
    try {
      const formData = new FormData();
      formData.append('file', decompressFile);
      if (decompressPassword) formData.append('password', decompressPassword);
      const response = await fetch('/api/decompress', {
        method: 'POST',
        body: formData,
      });

      let data: any = {};
      try {
        data = await response.json();
      } catch (jsonErr) {
        Modal.error({
          title: '解压失败',
          content: '服务器返回格式异常，无法解析结果',
        });
        message.error('解压失败，服务器返回格式异常');
        return;
      }

      console.log('decompress response', data);

      if (data && typeof data === 'object' && 'success' in data) {
        if (data.success) {
          Modal.success({
            title: '解压成功',
            content: data.message || '解压成功',
          });
          message.success(data.message || '解压成功');
        } else {
          Modal.error({
            title: '解压失败',
            content: data.message || '解压失败',
          });
          message.error(data.message || '解压失败');
        }
      } else {
        Modal.error({
          title: '解压失败',
          content: '服务器未返回预期结果',
        });
        message.error('解压失败，服务器未返回预期结果');
      }
    } catch (e: any) {
      Modal.error({
        title: '解压失败',
        content: (e && typeof e === 'object' && 'message' in e) ? e.message : '解压失败',
      });
      message.error('解压失败');
    } finally {
      setProcessing(false);
      setProgress(0);
    }
  };

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">文件压缩/解压</h1>
      <Tabs
        items={[
          {
            key: '1',
            label: '压缩文件',
            children: (
              <>
                <StyledDragger
                  name="files"
                  multiple
                  onChange={handleCompressUpload}
                  beforeUpload={() => false}
                  fileList={compressFileList}
                >
                  <p className="ant-upload-drag-icon">
                    <InboxOutlined />
                  </p>
                  <p className="ant-upload-text">点击或拖拽文件到此处</p>
                  <p className="ant-upload-hint">支持多个文件上传</p>
                </StyledDragger>
                {compressFiles.length > 0 && (
                  <ul style={{ margin: '10px 0', color: '#555' }}>
                    {compressFiles.map((file) => (
                      <li key={file.name}>{file.name}</li>
                    ))}
                  </ul>
                )}
                <OptionsWrapper>
                  <Radio.Group value={format} onChange={(e) => setFormat(e.target.value)}>
                    <Radio.Button value="zip">ZIP</Radio.Button>
                    <Radio.Button value="7z">7Z</Radio.Button>
                    <Radio.Button value="rar">RAR</Radio.Button>
                  </Radio.Group>
                  <Password
                    placeholder="输入压缩密码（可选）"
                    value={compressPassword}
                    onChange={e => setCompressPassword(e.target.value)}
                  />
                  <Button
                    type="primary"
                    onClick={handleCompress}
                    loading={processing}
                    disabled={compressFiles.length === 0}
                  >
                    开始压缩
                  </Button>
                </OptionsWrapper>
              </>
            ),
          },
          {
            key: '2',
            label: '解压文件',
            children: (
              <>
                <StyledDragger
                  name="file"
                  multiple={false}
                  onChange={handleDecompressUpload}
                  beforeUpload={() => false}
                  accept=".zip,.7z,.rar"
                  fileList={decompressFileList}
                  showUploadList={true}
                >
                  <p className="ant-upload-drag-icon">
                    <InboxOutlined />
                  </p>
                  <p className="ant-upload-text">点击或拖拽压缩包到此处</p>
                  <p className="ant-upload-hint">支持 ZIP、7Z、RAR 格式</p>
                </StyledDragger>
                {decompressFile && (
                  <ul style={{ margin: '10px 0', color: '#555' }}>
                    <li>{decompressFile.name}</li>
                  </ul>
                )}
                <OptionsWrapper>
                  <Password
                    placeholder="输入解压密码（如果有）"
                    value={decompressPassword}
                    onChange={e => setDecompressPassword(e.target.value)}
                  />
                  <Button
                    type="primary"
                    onClick={handleDecompress}
                    loading={processing}
                    disabled={!decompressFile}
                  >
                    开始解压
                  </Button>
                </OptionsWrapper>
              </>
            ),
          },
        ]}
      />
      {processing && (
        <Progress
          percent={progress}
          status={progress === 100 ? 'success' : 'active'}
        />
      )}
    </Container>
  );
};

export default Compress; 