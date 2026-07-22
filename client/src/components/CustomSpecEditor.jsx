import React from 'react';
import { Form, Input, Button, Space, Typography } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';

const CustomSpecEditor = () => {
  return (
    <Form.List name="custom_specs">
      {(fields, { add, remove }) => (
        <>
          <Typography.Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
            自定义规格 (可选，例如: 颜色, 材质)
          </Typography.Text>
          {fields.map(({ key, name, ...restField }) => (
            <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
              <Form.Item
                {...restField}
                name={[name, 'key']}
                rules={[{ required: true, message: '请输入规格名' }]}
              >
                <Input placeholder="规格名 (例: 颜色)" />
              </Form.Item>
              <Form.Item
                {...restField}
                name={[name, 'value']}
                rules={[{ required: true, message: '请输入数值' }]}
              >
                <Input placeholder="数值 (例: 黑色)" />
              </Form.Item>
              <DeleteOutlined onClick={() => remove(name)} style={{ color: '#ff4d4f' }} />
            </Space>
          ))}
          <Form.Item>
            <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
              添加规格
            </Button>
          </Form.Item>
        </>
      )}
    </Form.List>
  );
};

export default CustomSpecEditor;
