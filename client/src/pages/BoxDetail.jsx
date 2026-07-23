import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Table, Button, Space, message, Modal, Form, Input, InputNumber, Breadcrumb, Card, Descriptions, Tabs, Select, Typography } from 'antd';
import { PlusOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import axios from 'axios';

const { TabPane } = Tabs;

const BoxDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [box, setBox] = useState(null);
  const [products, setProducts] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('1');

  const fetchData = async () => {
    setLoading(true);
    try {
      const boxRes = await axios.get(`/api/boxes/single/${id}`);
      setBox(boxRes.data);

      const productsRes = await axios.get(`/api/box-products/${id}`);
      setProducts(productsRes.data);

      const invRes = await axios.get('/api/inventory');
      setInventory(invRes.data);
    } catch (err) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleSaveProduct = async (values) => {
    try {
      if (editingProduct) {
        await axios.put(`/api/box-products/${editingProduct.id}`, values);
        message.success('修改成功');
      } else {
        let payload;
        if (activeTab === '1') {
          // From Inventory
          const item = inventory.find(i => i.id === values.inventory_id);
          payload = {
            box_id: id,
            inventory_id: item.id,
            name: item.name,
            barcode: item.barcode,
            length_cm: item.length_cm,
            width_cm: item.width_cm,
            height_cm: item.height_cm,
            weight_g: item.weight_g,
            quantity: values.quantity
          };
        } else {
          // Manual Entry
          payload = {
            box_id: id,
            ...values
          };
        }
        await axios.post('/api/box-products', payload);
        message.success('添加成功');
      }
      setIsModalVisible(false);
      setEditingProduct(null);
      form.resetFields();
      fetchData();
    } catch (err) {
      console.error('Save failed:', err);
      message.error('保存失败');
    }
  };

  const showEditModal = (record) => {
    setEditingProduct(record);
    form.setFieldsValue(record);
    setActiveTab('2'); // Use manual input tab for editing existing records
    setIsModalVisible(true);
  };

  const columns = [
    { title: '编号', dataIndex: 'product_number', key: 'product_number' },
    { title: '名称', dataIndex: 'name', key: 'name' },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      render: (text) => <Typography.Text copyable>{text}</Typography.Text>
    },
    {
      title: '单品尺寸 (cm)',
      key: 'dimensions',
      render: (_, record) => (
        <Typography.Text copyable>{`${record.length_cm} x ${record.width_cm} x ${record.height_cm}`}</Typography.Text>
      )
    },
    {
      title: '单品重量 (g)',
      dataIndex: 'weight_g',
      key: 'weight_g',
      render: (text) => <Typography.Text copyable>{text}</Typography.Text>
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button icon={<EditOutlined />} size="small" onClick={() => showEditModal(record)}>编辑</Button>
          <Button icon={<DeleteOutlined />} danger size="small" onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  const handleDelete = (prodId) => {
    Modal.confirm({
      title: '确认删除',
      onOk: async () => {
        try {
          await axios.delete(`/api/box-products/${prodId}`);
          message.success('删除成功');
          fetchData();
        } catch (err) {
          message.error('删除失败');
        }
      },
    });
  };

  if (!box) return <div>加载中...</div>;

  return (
    <div>
      <Breadcrumb style={{ marginBottom: 16 }}>
        <Breadcrumb.Item><a onClick={() => navigate('/batches')}>批次列表</a></Breadcrumb.Item>
        <Breadcrumb.Item><a onClick={() => navigate(`/batches/${box.batch_id}`)}>批次详情</a></Breadcrumb.Item>
        <Breadcrumb.Item>第 {box.box_number} 箱</Breadcrumb.Item>
      </Breadcrumb>

      <Card style={{ marginBottom: 16 }}>
        <Descriptions title="箱子信息" bordered size="small">
          <Descriptions.Item label="箱号">第 {box.box_number} 箱</Descriptions.Item>
          <Descriptions.Item label="箱子规格">{box.length_cm} x {box.width_cm} x {box.height_cm} cm</Descriptions.Item>
          <Descriptions.Item label="整箱重量">{box.weight_kg} kg</Descriptions.Item>
        </Descriptions>
      </Card>

      <div style={{ marginBottom: 16, textAlign: 'right' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalVisible(true)}>添加产品</Button>
      </div>

      <Table
        columns={columns}
        dataSource={products}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editingProduct ? "编辑产品" : "添加产品到箱子"}
        open={isModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalVisible(false);
          setEditingProduct(null);
          form.resetFields();
        }}
        width={600}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          {!editingProduct && (
            <TabPane tab="从产品库选择" key="1">
              <Form form={form} layout="vertical" onFinish={handleSaveProduct}>
                <Form.Item name="inventory_id" label="选择产品" rules={[{ required: true }]}>
                  <Select
                    showSearch
                    placeholder="搜索名称/编码/条形码"
                    optionFilterProp="children"
                    filterOption={(input, option) =>
                      (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                    }
                    options={inventory.map(i => ({
                      value: i.id,
                      label: `${i.name} (${i.product_code || '无编码'}) - ${i.length_cm}x${i.width_cm}x${i.height_cm}cm`
                    }))}
                  />
                </Form.Item>
                <Form.Item name="quantity" label="数量" rules={[{ required: true }]}>
                  <InputNumber min={1} style={{ width: '100%' }} />
                </Form.Item>
              </Form>
            </TabPane>
          )}
          <TabPane tab={editingProduct ? "产品信息" : "手动输入"} key="2">
            <Form form={form} layout="vertical" onFinish={handleSaveProduct}>
              <Form.Item name="name" label="产品名称" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Form.Item name="barcode" label="条形码">
                <Input />
              </Form.Item>
              <Space>
                <Form.Item name="length_cm" label="长 (cm)" rules={[{ required: true }]}>
                  <InputNumber min={0.1} />
                </Form.Item>
                <Form.Item name="width_cm" label="宽 (cm)" rules={[{ required: true }]}>
                  <InputNumber min={0.1} />
                </Form.Item>
                <Form.Item name="height_cm" label="高 (cm)" rules={[{ required: true }]}>
                  <InputNumber min={0.1} />
                </Form.Item>
              </Space>
              <Form.Item name="weight_g" label="重量 (g)" rules={[{ required: true }]}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="quantity" label="数量" rules={[{ required: true }]}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Form>
          </TabPane>
        </Tabs>
      </Modal>
    </div>
  );
};

export default BoxDetail;
