import React, { useState, useEffect } from 'react';
import { Form, Input, InputNumber, Button, Space, Card, message, Spin, Breadcrumb } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import CustomSpecEditor from '../components/CustomSpecEditor';

const InventoryEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const isEdit = !!id;

  useEffect(() => {
    if (isEdit) {
      const fetchProduct = async () => {
        setLoading(true);
        try {
          const res = await axios.get(`/api/inventory/${id}`);
          const data = res.data;
          // Parse custom_specs if it's a string
          if (data.custom_specs) {
            data.custom_specs = JSON.parse(data.custom_specs);
          } else {
            data.custom_specs = [];
          }
          form.setFieldsValue(data);
        } catch (err) {
          message.error('获取产品详情失败');
          navigate('/inventory');
        } finally {
          setLoading(false);
        }
      };
      fetchProduct();
    }
  }, [id, isEdit, form, navigate]);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // Stringify custom_specs for backend
      const payload = {
        ...values,
        custom_specs: values.custom_specs ? JSON.stringify(values.custom_specs) : null,
      };

      if (isEdit) {
        await axios.put(`/api/inventory/${id}`, payload);
        message.success('更新成功');
      } else {
        await axios.post('/api/inventory', payload);
        message.success('创建成功');
      }
      navigate('/inventory');
    } catch (err) {
      message.error(err.response?.data?.error || '保存失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Breadcrumb style={{ marginBottom: 16 }}>
        <Breadcrumb.Item><a onClick={() => navigate('/inventory')}>产品库</a></Breadcrumb.Item>
        <Breadcrumb.Item>{isEdit ? '编辑产品' : '新建产品'}</Breadcrumb.Item>
      </Breadcrumb>

      <Card title={isEdit ? '编辑产品信息' : '录入新产品'}>
        <Spin spinning={loading}>
          <Form
            form={form}
            layout="vertical"
            onFinish={onFinish}
            initialValues={{ length_cm: 0, width_cm: 0, height_cm: 0, weight_g: 0, custom_specs: [] }}
          >
            <Space style={{ display: 'flex' }} align="baseline">
              <Form.Item
                name="product_code"
                label="产品编码"
                style={{ width: 200 }}
                rules={[{ max: 50, message: '最长 50 字符' }]}
              >
                <Input placeholder="可选" />
              </Form.Item>
              <Form.Item
                name="barcode"
                label="条形码"
                style={{ width: 250 }}
                rules={[{ pattern: /^[A-Za-z0-9]*$/, message: '仅支持字母和数字' }, { max: 50, message: '最长 50 字符' }]}
              >
                <Input placeholder="扫描或输入条形码" />
              </Form.Item>
            </Space>

            <Form.Item
              name="name"
              label="产品名称"
              rules={[
                { required: true, message: '请输入名称' },
                { whitespace: true, message: '不能仅包含空格' },
                { max: 200, message: '最长 200 字符' }
              ]}
            >
              <Input placeholder="输入产品名称" />
            </Form.Item>

            <div style={{ display: 'flex', gap: 16, marginBottom: 8 }}>
              <Form.Item name="length_cm" label="长 (cm)" rules={[{ required: true, type: 'number', min: 0.01 }]}>
                <InputNumber min={0.01} precision={2} />
              </Form.Item>
              <Form.Item name="width_cm" label="宽 (cm)" rules={[{ required: true, type: 'number', min: 0.01 }]}>
                <InputNumber min={0.01} precision={2} />
              </Form.Item>
              <Form.Item name="height_cm" label="高 (cm)" rules={[{ required: true, type: 'number', min: 0.01 }]}>
                <InputNumber min={0.01} precision={2} />
              </Form.Item>
              <Form.Item name="weight_g" label="重量 (g)" rules={[{ required: true, type: 'number', min: 0.01 }]}>
                <InputNumber min={0.01} precision={2} />
              </Form.Item>
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid #f0f0f0', margin: '24px 0' }} />

            <CustomSpecEditor />

            <Form.Item style={{ marginTop: 24 }}>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading}>
                  保存
                </Button>
                <Button onClick={() => navigate('/inventory')}>取消</Button>
              </Space>
            </Form.Item>
          </Form>
        </Spin>
      </Card>
    </div>
  );
};

export default InventoryEdit;
