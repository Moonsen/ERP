import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Space, message, Upload, Modal, Form, Input, Divider } from 'antd';
import { DownloadOutlined, UploadOutlined, ExclamationCircleOutlined, CloudSyncOutlined, SaveOutlined } from '@ant-design/icons';
import axios from 'axios';

const { Title, Paragraph } = Typography;

const DataManage = () => {
  const [loading, setLoading] = useState(false);
  const [syncLoading, setSyncLoading] = useState(false);
  const [stats, setStats] = useState({ total: 0, counts: {} });
  const [configForm] = Form.useForm();

  const fetchStats = async () => {
    try {
      const res = await axios.get('/api/backup/info');
      setStats(res.data);
    } catch (err) {
      console.error('Failed to fetch stats');
    }
  };

  useEffect(() => {
    fetchStats();
    // ... config loading logic
      try {
        const res = await axios.get('/api/sync/config');
        configForm.setFieldsValue({
          url: res.data.webdav_url,
          username: res.data.webdav_username,
          password: res.data.webdav_password,
        });
      } catch (err) {
        console.error('Failed to fetch config');
      }
    };
    fetchConfig();
  }, [configForm]);

  const handleSaveConfig = async (values) => {
    try {
      await axios.post('/api/sync/config', values);
      message.success('WebDAV 配置已保存');
    } catch (err) {
      message.error('保存配置失败');
    }
  };

  const handleSync = async () => {
    setSyncLoading(true);
    try {
      await axios.post('/api/sync/start');
      message.success('同步成功');
    } catch (err) {
      message.error('同步失败: ' + (err.response?.data?.error || err.message));
    } finally {
      setSyncLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const response = await axios.get('/api/backup/export', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      const filename = `warehouse-backup-${new Date().toISOString().slice(0, 10)}.json`;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      message.success('导出成功');
    } catch (err) {
      message.error('导出失败');
    }
  };

  const handleRestore = async (file) => {
    Modal.confirm({
      title: '确认恢复数据？',
      icon: <ExclamationCircleOutlined />,
      content: '恢复将覆盖当前全部数据，建议先导出当前数据备份。此操作不可撤销！',
      onOk: async () => {
        setLoading(true);
        const formData = new FormData();
        formData.append('file', file);
        try {
          await axios.post('/api/backup/restore', formData);
          message.success('数据恢复成功');
          window.location.reload();
        } catch (err) {
          message.error('恢复失败: ' + (err.response?.data?.error || err.message));
        } finally {
          setLoading(false);
        }
      },
    });
    return false; // Prevent automatic upload
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Title level={2}>数据管理</Title>

      <Card title={<span><CloudSyncOutlined /> 坚果云同步配置 (WebDAV)</span>} style={{ marginBottom: 24 }}>
        <Form form={configForm} layout="vertical" onFinish={handleSaveConfig}>
          <Form.Item name="url" label="WebDAV 服务器地址" rules={[{ required: true, message: '请输入服务器地址' }]}>
            <Input placeholder="例如: https://dav.jianguoyun.com/dav/" />
          </Form.Item>
          <Form.Item name="username" label="账号 (Email)" rules={[{ required: true, message: '请输入账号' }]}>
            <Input placeholder="坚果云登录邮箱" />
          </Form.Item>
          <Form.Item name="password" label="应用密码" rules={[{ required: true, message: '请输入应用密码' }]}>
            <Input.Password placeholder="在坚果云后台生成的第三方应用密码" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" icon={<SaveOutlined />} htmlType="submit">
                保存配置
              </Button>
              <Button type="default" icon={<CloudSyncOutlined />} onClick={handleSync} loading={syncLoading}>
                立即同步
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Divider />

      <Card title="本地数据备份" style={{ marginBottom: 24 }}>
        <Paragraph>
          将系统内所有数据（产品库、发货批次、箱子明细）导出为 JSON 文件，用于本地存档或数据迁移。
        </Paragraph>
        <Paragraph type="secondary">
          当前系统内共有 <Typography.Text strong>{stats.total}</Typography.Text> 条有效记录。
        </Paragraph>
        <Button type="primary" icon={<DownloadOutlined />} onClick={handleExport}>
          导出备份
        </Button>
      </Card>

      <Card title="数据恢复" headStyle={{ color: '#ff4d4f' }}>
        <Paragraph>
          <Typography.Text type="danger">警告：</Typography.Text>
          恢复操作会物理删除当前系统内的所有数据，并由备份文件中的内容替换。
        </Paragraph>
        <Upload beforeUpload={handleRestore} showUploadList={false}>
          <Button icon={<UploadOutlined />} danger loading={loading}>
            恢复备份
          </Button>
        </Upload>
      </Card>
    </div>
  );
};

export default DataManage;
