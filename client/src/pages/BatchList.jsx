import React, { useState, useEffect } from 'react';
import { Table, Button, Space, message, Modal, Form, Input } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowRightOutlined } from '@ant-design/icons';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const BatchList = () => {
  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingBatch, setEditingBatch] = useState(null);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const fetchBatches = async () => {
    setLoading(true);
    try {
      const res = await axios.get('/api/batches');
      setBatches(res.data);
    } catch (err) {
      message.error('获取批次列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBatches();
  }, []);

  const handleSave = async (values) => {
    try {
      if (editingBatch) {
        await axios.put(`/api/batches/${editingBatch.id}`, values);
        message.success('修改成功');
      } else {
        await axios.post('/api/batches', values);
        message.success('创建成功');
      }
      setIsModalVisible(false);
      setEditingBatch(null);
      form.resetFields();
      fetchBatches();
    } catch (err) {
      message.error('操作失败');
    }
  };

  const showEditModal = (record) => {
    setEditingBatch(record);
    form.setFieldsValue(record);
    setIsModalVisible(true);
  };

  const handleDelete = (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个批次吗？',
      onOk: async () => {
        try {
          await axios.delete(`/api/batches/${id}`);
          message.success('删除成功');
          fetchBatches();
        } catch (err) {
          message.error('删除失败');
        }
      },
    });
  };

  const columns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '目的地', dataIndex: 'destination', key: 'destination' },
    { title: '备注', dataIndex: 'remark', key: 'remark' },
    { title: '创建时间', dataIndex: 'created_at', key: 'created_at', render: (text) => new Date(text).toLocaleString() },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button icon={<ArrowRightOutlined />} onClick={() => navigate(`/batches/${record.id}`)}>进入批次</Button>
          <Button icon={<EditOutlined />} onClick={() => showEditModal(record)}>编辑</Button>
          <Button icon={<DeleteOutlined />} danger size="small" onClick={() => handleDelete(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, textAlign: 'right' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalVisible(true)}>新建批次</Button>
      </div>
      <Table
        columns={columns}
        dataSource={batches}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editingBatch ? "编辑发货批次" : "新建发货批次"}
        open={isModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalVisible(false);
          setEditingBatch(null);
          form.resetFields();
        }}
      >
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item name="name" label="批次名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input placeholder="例如: 2024-03-FBA-001" />
          </Form.Item>
          <Form.Item name="destination" label="目的地">
            <Input placeholder="例如: 美国 ONT8 仓库" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BatchList;
