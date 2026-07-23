let client = null;

async function getWebDAV() {
  return await import('webdav');
}

async function initClient(url, username, password) {
  const { createClient } = await getWebDAV();
  client = createClient(url, {
    username: username,
    password: password
  });
  return client;
}

function isInitialized() {
  return client !== null;
}

function getClient() {
  if (!client) {
    throw new Error('WebDAV client not initialized');
  }
  return client;
}

async function uploadFile(remotePath, content) {
  const c = getClient();
  await c.putFileContents(remotePath, JSON.stringify(content, null, 2));
}

async function downloadFile(remotePath) {
  const c = getClient();
  if (await c.exists(remotePath)) {
    const content = await c.getFileContents(remotePath, { format: 'text' });
    return JSON.parse(content);
  }
  return null;
}

module.exports = {
  initClient,
  isInitialized,
  getClient,
  uploadFile,
  downloadFile
};
