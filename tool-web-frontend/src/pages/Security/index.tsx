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
  const [file, setFile] = useState<File | null>(null);
  const [password, setPassword] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [qrContent, setQrContent] = useState('');
  const [qrColor, setQrColor] = useState('#000000');
  const [qrLogo, setQrLogo] = useState<File | null>(null);

  const handleFileUpload = (info: any) => {
    const { file } = info;
    setFile(file.originFileObj);
    message.success(`${file.name} 文件已选择`);
  };

  const handleLogoUpload = (info: any) => {
    const { file } = info;
    setQrLogo(file.originFileObj);
    message.success(`Logo 已选择`);
  };

  const handleEncrypt = async () => {
    if (!file || !password) {
      message.error('请选择文件并输入密码');
      return;
    }

    setProcessing(true);
    setProgress(0);

    // 模拟加密进度
    const timer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(timer);
          setProcessing(false);
          message.success('加密完成');
          return 100;
        }
        return prev + 10;
      });
    }, 500);
  };

  const handleDecrypt = async () => {
    if (!file || !password) {
      message.error('请选择文件并输入密码');
      return;
    }

    setProcessing(true);
    setProgress(0);

    // 模拟解密进度
    const timer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(timer);
          setProcessing(false);
          message.success('解密完成');
          return 100;
        }
        return prev + 10;
      });
    }, 500);
  };

  const handleGenerateQR = () => {
    if (!qrContent) {
      message.error('请输入二维码内容');
      return;
    }
    message.success('二维码生成成功');
  };

  const EncryptContent = () => (
    <>
      <StyledDragger
        name="file"
        multiple={false}
        onChange={handleFileUpload}
        beforeUpload={() => false}
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此处</p>
        <p className="ant-upload-hint">选择要加密的文件</p>
      </StyledDragger>

      <OptionsWrapper>
        <Password
          placeholder="输入加密密码"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          type="primary"
          onClick={handleEncrypt}
          loading={processing}
          disabled={!file || !password}
        >
          开始加密
        </Button>
      </OptionsWrapper>
    </>
  );

  const DecryptContent = () => (
    <>
      <StyledDragger
        name="file"
        multiple={false}
        onChange={handleFileUpload}
        beforeUpload={() => false}
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此处</p>
        <p className="ant-upload-hint">选择要解密的文件</p>
      </StyledDragger>

      <OptionsWrapper>
        <Password
          placeholder="输入解密密码"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          type="primary"
          onClick={handleDecrypt}
          loading={processing}
          disabled={!file || !password}
        >
          开始解密
        </Button>
      </OptionsWrapper>
    </>
  );

  const QRContent = () => (
    <>
      <OptionsWrapper>
        <TextArea
          rows={4}
          placeholder="输入要生成二维码的内容"
          value={qrContent}
          onChange={(e) => setQrContent(e.target.value)}
        />

        <div className="flex items-center gap-4">
          <span>二维码颜色：</span>
          <ColorPicker value={qrColor} onChange={(color) => setQrColor(color.toHexString())} />
        </div>

        <Upload
          accept="image/*"
          showUploadList={false}
          beforeUpload={() => false}
          onChange={handleLogoUpload}
        >
          <Button>上传 Logo</Button>
        </Upload>

        <Button
          type="primary"
          onClick={handleGenerateQR}
          disabled={!qrContent}
        >
          生成二维码
        </Button>
      </OptionsWrapper>

      <QRPreview>
        {qrContent ? '二维码预览区域' : '请输入内容'}
      </QRPreview>
    </>
  );

  const items = [
    {
      key: '1',
      label: '文件加密',
      children: <EncryptContent />,
    },
    {
      key: '2',
      label: '文件解密',
      children: <DecryptContent />,
    },
    {
      key: '3',
      label: '二维码生成',
      children: <QRContent />,
    },
  ];

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">安全工具</h1>
      
      <Tabs items={items} />

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