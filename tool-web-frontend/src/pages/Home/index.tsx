import React from 'react';
import { Card, Col, Row } from 'antd';
import { FileTextOutlined, FileZipOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

const StyledCard = styled(Card)`
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
`;

const IconWrapper = styled.div`
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 24px;
`;

const Home = () => {
  const tools = [
    {
      title: '格式转换',
      icon: <FileTextOutlined />,
      description: '支持多种文件格式转换，包括文档、图片等',
      link: '/convert',
    },
    {
      title: '压缩解压',
      icon: <FileZipOutlined />,
      description: '文件压缩和解压缩工具，支持多种压缩格式',
      link: '/compress',
    },
    {
      title: '安全工具',
      icon: <SafetyCertificateOutlined />,
      description: '文件加密解密、二维码生成等安全工具',
      link: '/security',
    },
  ];

  return (
    <div className="container mx-auto px-4">
      <h1 className="text-3xl font-bold text-center mb-8">在线工具集合</h1>
      <Row gutter={[24, 24]}>
        {tools.map((tool) => (
          <Col key={tool.title} xs={24} sm={12} md={8}>
            <Link to={tool.link}>
              <StyledCard>
                <IconWrapper>{tool.icon}</IconWrapper>
                <h2 className="text-xl font-bold mb-4">{tool.title}</h2>
                <p className="text-gray-600">{tool.description}</p>
              </StyledCard>
            </Link>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default Home; 