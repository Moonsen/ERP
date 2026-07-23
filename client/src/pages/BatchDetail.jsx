import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Table, Button, Space, message, Modal, Form, InputNumber, Breadcrumb, Card, Descriptions, Typography } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowRightOutlined, ArrowLeftOutlined, EditOutlined } from '@ant-design/icons';
import axios from 'axios';

const BatchDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [batch, setBatch] = useState(null);
  const [boxes, setBoxes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingBox, setEditingBox] = useState(null);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const batchRes = await axios.get(`/api/batches/${id}`);
      setBatch(batchRes.data);

      const boxesRes = await axios.get(`/api/boxes/${id}`);
      setBoxes(boxesRes.data);
    } catch (err) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleSaveBox = async (values) => {
    try {
      if (editingBox) {
        await axios.put(`/api/boxes/${editingBox.id}`, values);
        message.success('修改成功');
      } else {
        await axios.post('/api/boxes', { ...values, batch_id: id });
        message.success('添加箱子成功');
      }
      setIsModalVisible(false);
      setEditingBox(null);
      form.resetFields();
      fetchData();
    } catch (err) {
      message.error('保存失败');
    }
  };

  const showEditBoxModal = (record) => {
    setEditingBox(record);
    form.setFieldsValue(record);
    setIsModalVisible(true);
  };

  const handleDeleteBox = (boxId) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个箱子吗？',
      onOk: async () => {
        try {
          await axios.delete(`/api/boxes/${boxId}`);
          message.success('删除成功');
          fetchData();
        } catch (err) {
          message.error('删除失败');
        }
      },
    });
  };

  const columns = [
    { title: '箱号', dataIndex: 'box_number', key: 'box_number', render: (num) => `第 ${num} 箱` },
    {
      title: '尺寸 (cm)',
      key: 'dimensions',
      render: (_, record) => (
        <Typography.Text copyable>{`${record.length_cm} x ${record.width_cm} x ${record.height_cm}`}</Typography.Text>
      )
    },
    {
      title: '重量 (kg)',
      dataIndex: 'weight_kg',
      key: 'weight_kg',
      render: (text) => <Typography.Text copyable>{text}</Typography.Text>
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button icon={<ArrowRightOutlined />} onClick={() => navigate(`/boxes/${record.id}`)}>装箱明细</Button>
          <Button icon={<EditOutlined />} size="small" onClick={() => showEditBoxModal(record)}>编辑</Button>
          <Button icon={<DeleteOutlined />} danger size="small" onClick={() => handleDeleteBox(record.id)}>删除</Button>
        </Space>
      ),
    },
  ];

  const calculateStats = () => {
    const totalWeight = boxes.reduce((sum, box) => sum + (box.weight_kg || 0), 0);
    const totalVolumeCm = boxes.reduce((sum, box) => sum + (box.length_cm * box.width_cm * box.height_cm), 0);
    const totalVolumeM3 = totalVolumeCm / 1000000;
    return {
      weight: totalWeight.toFixed(2),
      volumeCm: totalVolumeCm.toFixed(2),
      volumeM3: totalVolumeM3.toFixed(2)
    };
  };

  const stats = calculateStats();

  if (!batch) return <div>加载中...</div>;

  return (
    <div>
      <Breadcrumb style={{ marginBottom: 16 }}>
        <Breadcrumb.Item><a onClick={() => navigate('/batches')}>批次列表</a></Breadcrumb.Item>
        <Breadcrumb.Item>{batch.name}</Breadcrumb.Item>
      </Breadcrumb>

      <Card style={{ marginBottom: 16 }}>
        <Descriptions title="批次统计信息" bordered size="small" column={2}>
          <Descriptions.Item label="批次名称">{batch.name}</Descriptions.Item>
          <Descriptions.Item label="目的地">{batch.destination || '未设置'}</Descriptions.Item>
          <Descriptions.Item label="总重量 (kg)">
            <Typography.Text strong type="warning">{stats.weight}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="总体积 (cm³)">
            <Typography.Text strong type="success">{stats.volumeCm}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="总体积 (m³)">
            <Typography.Text strong type="danger">{stats.volumeM3}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="备注">{batch.remark || '无'}</Descriptions.Item>
        </Descriptions>
      </Card>

      <div style={{ marginBottom: 16, textAlign: 'right' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalVisible(true)}>添加箱子</Button>
      </div>

      <Table
        columns={columns}
        dataSource={boxes}
        rowKey="id"
        loading={loading}
      />

      <Modal
        title={editingBox ? "编辑箱子" : "添加箱子"}
        open={isModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalVisible(false);
          setEditingBox(null);
          form.resetFields();
        }}
      >
        <Form form={form} layout="vertical" onFinish={handleSaveBox} initialValues={{ length_cm: 50, width_cm: 40, height_cm: 30, weight_kg: 10 }}>
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
          <Form.Item name="weight_kg" label="重量 (kg)" rules={[{ required: true }]}>
            <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BatchDetail;
