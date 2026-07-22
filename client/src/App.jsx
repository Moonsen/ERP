import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import {
  DatabaseOutlined,
  ContainerOutlined,
  CloudSyncOutlined,
} from '@ant-design/icons';

import InventoryList from './pages/InventoryList';
import InventoryEdit from './pages/InventoryEdit';
import BatchList from './pages/BatchList';
import BatchDetail from './pages/BatchDetail';
import BoxDetail from './pages/BoxDetail';
import DataManage from './pages/DataManage';

const App = () => {
  const location = useLocation();

  const menuItems = [
    {
      key: '/inventory',
      icon: <DatabaseOutlined />,
      label: <Link to="/inventory">产品库</Link>,
    },
    {
      key: '/batches',
      icon: <ContainerOutlined />,
      label: <Link to="/batches">发货批次</Link>,
    },
    {
      key: '/data',
      icon: <CloudSyncOutlined />,
      label: <Link to="/data">数据管理</Link>,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', color: '#fff', textAlign: 'center', lineHeight: '32px', fontWeight: 'bold' }}>
          ERP 系统
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={[location.pathname]} items={menuItems} />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: 0 }} />
        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ padding: 24, minHeight: 360, background: '#fff' }}>
            <Routes>
              <Route path="/inventory" element={<InventoryList />} />
              <Route path="/inventory/new" element={<InventoryEdit />} />
              <Route path="/inventory/edit/:id" element={<InventoryEdit />} />
              <Route path="/batches" element={<BatchList />} />
              <Route path="/batches/:id" element={<BatchDetail />} />
              <Route path="/boxes/:id" element={<BoxDetail />} />
              <Route path="/data" element={<DataManage />} />
              <Route path="/" element={<InventoryList />} />
            </Routes>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

const Root = () => (
  <Router>
    <App />
  </Router>
);

export default Root;
