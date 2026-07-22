import React, { useState, useEffect } from 'react';
import { Table, Button, Input, Space, message, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const InventoryList = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const navigate = useNavigate();

  const fetchProducts = async (search = '') => {
    setLoading(true);
    try {
      const res = await axios.get(`/api/inventory?search=${search}`);
      setProducts(res.data);
    } catch (err) {
      message.error('获取产品列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleDelete = (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个产品吗？此操作不可恢复。',
      onOk: async () => {
        try {
          await axios.delete(`/api/inventory/${id}`);
          message.success('删除成功');
          fetchProducts(searchText);
        } catch (err) {
          message.error('删除失败');
        }
      },
    });
  };

  const columns = [
    { title: '编码', dataIndex: 'product_code', key: 'product_code' },
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '条形码', dataIndex: 'barcode', key: 'barcode' },
    {
      title: '尺寸 (cm)',
      key: 'dimensions',
      render: (_, record) => `${record.length_cm} x ${record.width_cm} x ${record.height_cm}`
    },
    { title: '重量 (g)', dataIndex: 'weight_g', key: 'weight_g' },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button icon={<EditOutlined />} size="small" onClick={() => navigate(`/inventory/edit/${record.id}`)}>编辑</Button>
          <Button icon={<DeleteOutlined />} danger size="small" onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Input
            placeholder="搜索名称/编码/条形码"
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            onPressEnter={() => fetchProducts(searchText)}
            style={{ width: 300 }}
          />
          <Button type="primary" onClick={() => fetchProducts(searchText)}>搜索</Button>
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/inventory/new')}>新建产品</Button>
      </div>
      <Table
        columns={columns}
        dataSource={products}
        rowKey="id"
        loading={loading}
      />
    </div>
  );
};

export default InventoryList;
