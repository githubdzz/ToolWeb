import { Layout as AntLayout, Menu } from 'antd';
import { Link, Outlet, useLocation } from 'react-router-dom';
import styled from 'styled-components';

const { Header, Content, Footer } = AntLayout;

const StyledLayout = styled(AntLayout)`
  min-height: 100vh;
`;

const StyledHeader = styled(Header)`
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
`;

const Logo = styled(Link)`
  font-size: 20px;
  font-weight: bold;
  color: #1890ff;
  margin-right: 48px;
  text-decoration: none;
`;

const StyledContent = styled(Content)`
  padding: 24px;
  background: #fff;
`;

const StyledFooter = styled(Footer)`
  text-align: center;
  background: #f0f2f5;
`;

const Layout = () => {
  const location = useLocation();

  const menuItems = [
    { key: '/', label: '首页' },
    { key: '/convert', label: '格式转换' },
    { key: '/compress', label: '压缩解压' },
    { key: '/security', label: '安全工具' },
  ];

  return (
    <StyledLayout>
      <StyledHeader>
        <Logo to="/">工具网站</Logo>
        <Menu
          mode="horizontal"
          selectedKeys={[location.pathname]}
          items={menuItems.map((item) => ({
            ...item,
            label: <Link to={item.key}>{item.label}</Link>,
          }))}
        />
      </StyledHeader>
      <StyledContent>
        <Outlet />
      </StyledContent>
      <StyledFooter>
        ©{new Date().getFullYear()} 工具网站 All Rights Reserved
      </StyledFooter>
    </StyledLayout>
  );
};

export default Layout; 