import { useState } from 'react';
import { Upload, Radio, Input, Button, Progress, message, Tabs } from 'antd';
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
  const [files, setFiles] = useState<File[]>([]);
  const [format, setFormat] = useState('zip');
  const [password, setPassword] = useState('');
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);

  const handleUpload = (info: any) => {
    const fileList = info.fileList.map((file: any) => file.originFileObj);
    setFiles(fileList);
    message.success(`${fileList.length} 个文件已选择`);
  };

  const handleCompress = async () => {
    if (files.length === 0) {
      message.error('请选择要压缩的文件');
      return;
    }

    setProcessing(true);
    setProgress(0);

    // 模拟压缩进度
    const timer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(timer);
          setProcessing(false);
          message.success('压缩完成');
          return 100;
        }
        return prev + 10;
      });
    }, 500);
  };

  const handleDecompress = async () => {
    if (files.length === 0) {
      message.error('请选择要解压的文件');
      return;
    }

    setProcessing(true);
    setProgress(0);

    // 模拟解压进度
    const timer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(timer);
          setProcessing(false);
          message.success('解压完成');
          return 100;
        }
        return prev + 10;
      });
    }, 500);
  };

  const CompressContent = () => (
    <>
      <StyledDragger
        name="files"
        multiple
        onChange={handleUpload}
        beforeUpload={() => false}
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此处</p>
        <p className="ant-upload-hint">支持多个文件上传</p>
      </StyledDragger>

      <OptionsWrapper>
        <Radio.Group value={format} onChange={(e) => setFormat(e.target.value)}>
          <Radio.Button value="zip">ZIP</Radio.Button>
          <Radio.Button value="7z">7Z</Radio.Button>
          <Radio.Button value="rar">RAR</Radio.Button>
        </Radio.Group>

        <Password
          placeholder="输入压缩密码（可选）"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          type="primary"
          onClick={handleCompress}
          loading={processing}
          disabled={files.length === 0}
        >
          开始压缩
        </Button>
      </OptionsWrapper>
    </>
  );

  const DecompressContent = () => (
    <>
      <StyledDragger
        name="file"
        multiple={false}
        onChange={handleUpload}
        beforeUpload={() => false}
        accept=".zip,.7z,.rar"
      >
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽压缩包到此处</p>
        <p className="ant-upload-hint">支持 ZIP、7Z、RAR 格式</p>
      </StyledDragger>

      <OptionsWrapper>
        <Password
          placeholder="输入解压密码（如果有）"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          type="primary"
          onClick={handleDecompress}
          loading={processing}
          disabled={files.length === 0}
        >
          开始解压
        </Button>
      </OptionsWrapper>
    </>
  );

  const items = [
    {
      key: '1',
      label: '压缩文件',
      children: <CompressContent />,
    },
    {
      key: '2',
      label: '解压文件',
      children: <DecompressContent />,
    },
  ];

  return (
    <Container>
      <h1 className="text-2xl font-bold mb-6">文件压缩/解压</h1>
      
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

export default Compress; 