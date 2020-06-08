package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.tuweni.bytes.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkData {
  private ArrayList<Integer> chunkStartAddresses;
  private byte[] code;
  private int threshold;
  private boolean startAddressesAsKeys; // Indicates whether start addresses to be used as keys or not.
  private Map<Integer, Bytes> keyValueMap;

  ChunkData(ArrayList<Integer> chunkStartAddresses, Bytes code, boolean startAddressesAsKeys, int threshold) {
    this.chunkStartAddresses = chunkStartAddresses;
    this.code = code.toArray();
    this.startAddressesAsKeys = startAddressesAsKeys;
    this.threshold = threshold;
    keyValueMap = new HashMap<> ();
  }

  /*
   * This procedure constructs a key value map and returns it. The values are used to construct the leaf nodes
   * of Merkle Patricia Trie.
   */
  public Map<Integer, Bytes> constructKeyMap() {
    // 2 cases.
    if(startAddressesAsKeys) {
      // Case 1. Start addresses are used as keys
      int size = chunkStartAddresses.size();
      for(int i = 0; i < size; i ++) {
        int length;
        if(i < size - 1) {
          length = chunkStartAddresses.get(i+1) - chunkStartAddresses.get(i);
        } else {
          length = code.length - chunkStartAddresses.get(i);
        }
        Bytes chunk = Bytes.wrap(code, chunkStartAddresses.get(i), length);
        keyValueMap.put(chunkStartAddresses.get(i), chunk);
      }
    } else {
      // Case 2. Start addresses are not used as keys
      for(int i = 0, addr = 0; i < chunkStartAddresses.size(); i ++, addr += threshold) {
        // The chunk size is threshold + 1, with the first byte recording the start address offset
        // inside the chunk. This tides over the pushdata
        byte startAddressOffset = chunkStartAddresses.get(i).byteValue();
        Bytes chunk = Bytes.concatenate(
          Bytes.of(startAddressOffset),
          Bytes.wrap(code, addr, threshold));
        keyValueMap.put(i, chunk);
      }
    }
    return keyValueMap;
  }
}
