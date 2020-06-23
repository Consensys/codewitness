/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.poc.witnesscodeanalysis.common;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.logging.log4j.LogManager.getLogger;

public class ChunkData {
  private static final Logger LOG = getLogger();
  public static boolean INCLUDECODE = false;

  int id;
  private ArrayList<Integer> chunkStartAddresses;
  private byte[] code;
  private int codeLength;
  private int threshold;
  private boolean startAddressesAsKeys; // Indicates whether start addresses to be used as keys or not.
  private Map<Integer, Bytes> keyValueMap;

  public ChunkData(int id, ArrayList<Integer> chunkStartAddresses, Bytes code, boolean startAddressesAsKeys, int threshold) {
    this.id = id;
    this.chunkStartAddresses = chunkStartAddresses;
    if (INCLUDECODE) {
      this.code = code.toArray();
    }
    this.codeLength = code.size();
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

  public int getId() {
    return id;
  }

  public ArrayList<Integer> getChunkStartAddresses() {
    return chunkStartAddresses;
  }

  public Map<Integer, Integer> getChunks() {
    Map<Integer, Integer> map = new TreeMap<>();
    int size = this.chunkStartAddresses.size();
    if (size == 0) {
      return map;
    }
    int previousStart = 0;
    for (int i=1; i < size; i++) {
      previousStart = this.chunkStartAddresses.get(i-1);
      int length = this.chunkStartAddresses.get(i) - previousStart;
      map.put(previousStart, length);
    }
    previousStart = this.chunkStartAddresses.get(size-1);
    map.put(previousStart, this.codeLength - previousStart);
    return map;
  }

  public byte[] getCode() {
    return code;
  }

  public int getCodeLength() {
    return codeLength;
  }

  public int getThreshold() {
    return threshold;
  }

  public boolean isStartAddressesAsKeys() {
    return startAddressesAsKeys;
  }

  public Map<Integer, Bytes> getKeyValueMap() {
    return keyValueMap;
  }
}