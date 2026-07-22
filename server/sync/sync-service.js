const db = require('../db');
const webdav = require('./webdav-client');
const { mergeRecords } = require('./merge');

const REMOTE_FILE = '/warehouse_erp/sync_data.json';
const MACHINE_ID = 'local-web-server';

async function performSync() {
  console.log('Starting sync...');

  try {
    // 1. Download remote data
    let remoteData = await webdav.downloadFile(REMOTE_FILE);
    if (!remoteData) {
      remoteData = {
        batches: [],
        boxes: [],
        productInventory: [],
        boxProducts: []
      };
    }

    // 2. Get local data
    const localBatches = db.prepare('SELECT * FROM batch').all();
    const localBoxes = db.prepare('SELECT * FROM box').all();
    const localInv = db.prepare('SELECT * FROM product_inventory').all();
    const localBoxProds = db.prepare('SELECT * FROM box_product').all();

    // 3. Merge each table
    const batchResult = mergeRecords(localBatches, remoteData.batches);
    const boxResult = mergeRecords(localBoxes, remoteData.boxes);
    const invResult = mergeRecords(localInv, remoteData.productInventory);
    const boxProdResult = mergeRecords(localBoxProds, remoteData.boxProducts);

    // 4. Update local DB with changes from remote
    const transaction = db.transaction(() => {
      // Helper to update/insert a record
      const upsert = (table, record, columns) => {
        const placeholders = columns.map(() => '?').join(',');
        const colString = columns.join(',');
        const updateString = columns.map(c => `${c} = excluded.${c}`).join(',');

        const sql = `
          INSERT INTO ${table} (${colString})
          VALUES (${placeholders})
          ON CONFLICT(id) DO UPDATE SET ${updateString}
        `;
        const values = columns.map(c => record[c]);
        db.prepare(sql).run(...values);
      };

      batchResult.toUpdateLocal.forEach(r => upsert('batch', r, ['id', 'name', 'destination', 'remark', 'created_at', 'updated_at', 'deleted_at', 'machine_id']));
      boxResult.toUpdateLocal.forEach(r => upsert('box', r, ['id', 'batch_id', 'box_number', 'length_cm', 'width_cm', 'height_cm', 'weight_kg', 'created_at', 'updated_at', 'deleted_at', 'machine_id']));
      invResult.toUpdateLocal.forEach(r => upsert('product_inventory', r, ['id', 'product_code', 'name', 'barcode', 'length_cm', 'width_cm', 'height_cm', 'weight_g', 'custom_specs', 'created_at', 'updated_at', 'deleted_at', 'machine_id']));
      boxProdResult.toUpdateLocal.forEach(r => upsert('box_product', r, ['id', 'box_id', 'inventory_id', 'product_number', 'name', 'barcode', 'length_cm', 'width_cm', 'height_cm', 'weight_g', 'quantity', 'created_at', 'updated_at', 'deleted_at', 'machine_id']));
    });

    transaction();

    // 5. Upload merged data back to remote
    const finalData = {
      batches: batchResult.merged,
      boxes: boxResult.merged,
      productInventory: invResult.merged,
      boxProducts: boxProdResult.merged,
      lastSyncTime: new Date().toISOString()
    };

    await webdav.uploadFile(REMOTE_FILE, finalData);
    console.log('Sync completed successfully');
    return { success: true };

  } catch (error) {
    console.error('Sync failed:', error);
    throw error;
  }
}

module.exports = { performSync };
