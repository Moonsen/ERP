/**
 * Last-Write-Wins (LWW) Merge Strategy
 * Merges remote data into local database records.
 */
function mergeRecords(localRecords, remoteRecords) {
  const localMap = new Map(localRecords.map(r => [r.id, r]));
  const remoteMap = new Map(remoteRecords.map(r => [r.id, r]));

  const allIds = new Set([...localMap.keys(), ...remoteMap.keys()]);
  const results = [];
  const toUpdateLocal = [];

  for (const id of allIds) {
    const local = localMap.get(id);
    const remote = remoteMap.get(id);

    if (!local) {
      // New from remote
      results.push(remote);
      toUpdateLocal.push(remote);
    } else if (!remote) {
      // Only local has it
      results.push(local);
    } else {
      // Conflict: Compare updated_at
      const localTime = new Date(local.updated_at).getTime();
      const remoteTime = new Date(remote.updated_at).getTime();

      if (remoteTime > localTime) {
        results.push(remote);
        toUpdateLocal.push(remote);
      } else {
        results.push(local);
      }
    }
  }

  return { merged: results, toUpdateLocal };
}

module.exports = { mergeRecords };
