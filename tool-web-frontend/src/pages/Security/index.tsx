import { useState } from 'react';
import { Tabs, Upload, Input, Button, Progress, message, ColorPicker } from 'antd';
import { InboxOutlined } from '@ant-design/icons';
import styled from 'styled-components';

const { Dragger } = Upload;
const { Password, TextArea } = Input;

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

const QRPreview = styled.div`
  width: 200px;
  height: 200px;
  margin: 24px auto;
  border: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const Security = () => {
  // 加密Tab相关state
  const [encryptFile, setEncryptFile] = useState<File | null>(null);
  const [encryptFileList, setEncryptFileList] = useState<any[]>([]);
  const [encryptPassword, setEncryptPassword] = useState('');
  // 解密Tab相关state
  const [decryptFile, setDecryptFile] = useState<File | null>(null);
  const [decryptFileList, setDecryptFileList] = useState<any[]>([]);
  const [decryptPassword, setDecryptPassword] = useState('');
  // 二维码Tab相关state
  const [qrContent, setQrContent] = useState('');
  const [qrColor, setQrColor] = useState('#000000');
  const [qrLogo, setQrLogo] = useState<File | null>(null);
  const [qrLogoFileList, setQrLogoFileList] = useState<any[]>([]);
  const [qrImageUrl, setQrImageUrl] = useState('');
  // 通用
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);

  // 加密Tab上传
  const handleEncryptUpload = (info: any) => {
    setEncryptFileList(info.fileList);
    const file = info.fileList[0]?.originFileObj || info.fileList[0];
    setEncryptFile(file || null);
    if (file) {
      message.success(`${file.name} 已选择`);
    }
  };

  // 解密Tab上传
  const handleDecryptUpload = (info: any) => {
    setDecryptFileList(info.fileList);
    const file = info.fileList[0]?.originFileObj || info.fileList[0];
    setDecryptFile(file || null);
    if (file) {
      message.success(`${file.name} 已选择`);
    }
  };

  // 二维码logo上传
  const handleLogoUpload = (info: any) => {
    setQrLogoFileList(info.fileList);
    const file = info.fileList[0]?.originFileObj || info.fileList[0];
    setQrLogo(file || null);
    if (file) {
      message.success('Logo 已选择');
    }
  };

  const handleEncrypt = async () => {
    if (!encryptFile || !encryptPassword) {
      message.error('请选择文件并输入密码');
      return;
    }
    setProcessing(true);
    setProgress(0);
    try {
      const formData = new FormData();
      formData.append('file', encryptFile);
      formData.append('password', encryptPassword);
      const response = await fetch('/api/encrypt', {
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
        message.success('加密完成，已自动下载');
      } else {
        message.success(data.message || '加密完成');
      }
    } catch (e) {
      message.error('加密失败');
    } finally {
      setProcessing(false);
      setProgress(0);
    }
  };

  const handleDecrypt = async () => {
    if (!decryptFile || !decryptPassword) {
      message.error('请选择文件并输入密码');
      return;
    }
    setProcessing(true);
    setProgress(0);
    try {
      const formData = new FormData();
      formData.append('file', decryptFile);
      formData.append('password', decryptPassword);
      const response = await fetch('/api/decrypt', {
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
        message.success('解密完成，已自动下载');
      } else {
        message.success(data.message || '解密完成');
      }
    } catch (e) {
      message.error('解密失败');
    } finally {
      setProcessing(false);
      setProgress(0);
    }
  };

  const handleGenerateQR = async () => {
    if (!qrContent) {
      message.error('请输入二维码内容');
      return;
    }
    setProcessing(true);
    try {
      const formData = new FormData();
      formData.append('content', qrContent);
      formData.append('color', qrColor);
      if (qrLogo) formData.append('logo', qrLogo);
      const response = await fetch('/api/qrcode', {
        method: 'POST',
        body: formData,
      });
      const data = await response.json();
      if (data.url) {
        setQrImageUrl(data.url);
        message.success('二维码生成成功');
      } else {
        message.success(data.message || '二维码生成成功');
      }
    } catch (e) {
      message.error('二维码生成失败');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">安全工具</h1>
      <Tabs
        items={[
          {
            key: '1',
            label: '文件加密',
            children: (
              <>
                <StyledDragger
                  name="file"
                  multiple={false}
                  onChange={handleEncryptUpload}
                  beforeUpload={() => false}
                  fileList={encryptFileList}
                  showUploadList={true}
                >
                  <p className="ant-upload-drag-icon">
                    <InboxOutlined />
                  </p>
                  <p className="ant-upload-text">点击或拖拽文件到此处</p>
                  <p className="ant-upload-hint">选择要加密的文件</p>
                </StyledDragger>
                {encryptFile && (
                  <ul style={{ margin: '10px 0', color: '#555' }}>
                    <li>{encryptFile.name}</li>
                  </ul>
                )}
                <OptionsWrapper>
                  <Password
                    placeholder="输入加密密码"
                    value={encryptPassword}
                    onChange={e => setEncryptPassword(e.target.value)}
                  />
                  <Button
                    type="primary"
                    onClick={handleEncrypt}
                    loading={processing}
                    disabled={!encryptFile || !encryptPassword}
                  >
                    开始加密
                  </Button>
                </OptionsWrapper>
              </>
            ),
          },
          {
            key: '2',
            label: '文件解密',
            children: (
              <>
                <StyledDragger
                  name="file"
                  multiple={false}
                  onChange={handleDecryptUpload}
                  beforeUpload={() => false}
                  fileList={decryptFileList}
                  showUploadList={true}
                >
                  <p className="ant-upload-drag-icon">
                    <InboxOutlined />
                  </p>
                  <p className="ant-upload-text">点击或拖拽文件到此处</p>
                  <p className="ant-upload-hint">选择要解密的文件</p>
                </StyledDragger>
                {decryptFile && (
                  <ul style={{ margin: '10px 0', color: '#555' }}>
                    <li>{decryptFile.name}</li>
                  </ul>
                )}
                <OptionsWrapper>
                  <Password
                    placeholder="输入解密密码"
                    value={decryptPassword}
                    onChange={e => setDecryptPassword(e.target.value)}
                  />
                  <Button
                    type="primary"
                    onClick={handleDecrypt}
                    loading={processing}
                    disabled={!decryptFile || !decryptPassword}
                  >
                    开始解密
                  </Button>
                </OptionsWrapper>
              </>
            ),
          },
          {
            key: '3',
            label: '二维码生成',
            children: (
              <>
                <OptionsWrapper>
                  <TextArea
                    rows={4}
                    placeholder="输入要生成二维码的内容"
                    value={qrContent}
                    onChange={e => setQrContent(e.target.value)}
                  />
                  <div className="flex items-center gap-4">
                    <span>二维码颜色：</span>
                    <ColorPicker value={qrColor} onChange={(color) => setQrColor(color.toHexString())} />
                  </div>
                  <Upload
                    accept="image/*"
                    showUploadList={true}
                    beforeUpload={() => false}
                    onChange={handleLogoUpload}
                    fileList={qrLogoFileList}
                  >
                    <Button>上传 Logo</Button>
                  </Upload>
                  <Button
                    type="primary"
                    onClick={handleGenerateQR}
                    disabled={!qrContent}
                    loading={processing}
                  >
                    生成二维码
                  </Button>
                </OptionsWrapper>
                <QRPreview>
                  {qrImageUrl ? (
                    <img src={qrImageUrl} alt="二维码" style={{ width: 180, height: 180 }} />
                  ) : (
                    qrContent ? '二维码预览区域' : '请输入内容'
                  )}
                </QRPreview>
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

export default Security; 