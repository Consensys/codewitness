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
package tech.pegasys.poc.witnesscodeanalysis.fixed;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.poc.witnesscodeanalysis.CodeAnalysisBase;
import tech.pegasys.poc.witnesscodeanalysis.common.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.MultiMerkleProof;
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.SimpleMerklePatriciaTrie;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.MerklePatriciaTrie;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Split the code at the next operation boundary after a certain threshold.
 */
public class FixedSizeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  private int threshold;
  private ArrayList<Bytes> chunkStartAddresses;
  private MerklePatriciaTrie<Bytes, Bytes> codeTrie;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public FixedSizeAnalysis(Bytes code, int threshold) {
    super(code);
    this.threshold = threshold;
    chunkStartAddresses = null;
  }

  /*
   * This method creates chunks of the given code and populates the chunkStartAddresses with the
   * start addresses of the chunks.
   */
  public void createChunks() {
    short pc = 0;
    int currentChunkSize = 0;
    chunkStartAddresses = new ArrayList<>();
    chunkStartAddresses.add(Bytes.of(ByteBuffer.allocate(2).putShort(pc).array()));

    int codeLength = this.code.size();

    // True when the part of the contract being processed is definitely code.
    boolean executableCodeSection = true;

    LOG.trace(" Contract size: {}", codeLength);
    while (pc < codeLength) {
      if (executableCodeSection) {
        final Operation curOp = registry.get(code.get(pc), 0);
        if (curOp == null) {
          LOG.trace(" Found unknown opcode at PC: {}", PcUtils.pcStr(pc));
          executableCodeSection = false;
          // Move the PC to the end of the chunk.
          pc += this.threshold - pc % this.threshold;
          continue;
        }
        int opSize = curOp.getOpSize();

        if(currentChunkSize + opSize >= this.threshold) {
          currentChunkSize = 0;
          pc += opSize;
          chunkStartAddresses.add(Bytes.of(ByteBuffer.allocate(2).putShort(pc).array()));
        }
        else {
          currentChunkSize += opSize;
          pc += opSize;
        }
      }
      else {
        // processing non-executable code
        chunkStartAddresses.add(Bytes.of(ByteBuffer.allocate(2).putShort(pc).array()));
        pc += this.threshold;
      }
    }

    LOG.trace(" Fixed Analysis found chunk starting addresses: ");
    for(Bytes e : chunkStartAddresses) {
      LOG.trace(PcUtils.pcStr(e.toInt()));
    }
    LOG.trace("  Finished. {} chunks", chunkStartAddresses.size());
  }

  /*
   * This method uses the chunk start addresses to create a SimpleMerklePatriciaTrie with
   * plain natural number addresses as keys, and code chunks as values
   */
  public void merkelize() {
    codeTrie = new SimpleMerklePatriciaTrie<>(v->v);
    int numChunks = chunkStartAddresses.size();
    // The keys are chunk start addresses
    for (int i=0; i < numChunks; i++) {
      int thisChunkStart = chunkStartAddresses.get(i).toInt();

      int length = (i == numChunks - 1) ? code.size() - thisChunkStart :
        chunkStartAddresses.get(i+1).toInt() - thisChunkStart;

      Bytes chunk = this.code.slice(thisChunkStart, length);
      codeTrie.put(chunkStartAddresses.get(i), chunk);
    }
    LOG.trace("Merkelization finished.");
  }

  /*
   * This method constructs proof and prints some statistics
   */
  public void computeMultiproofTest() {
    List<Bytes> testKeys = new ArrayList<>();
    Random rand = new Random();
    testKeys.add(chunkStartAddresses.get(rand.nextInt(chunkStartAddresses.size())));
    testKeys.add(chunkStartAddresses.get(rand.nextInt(chunkStartAddresses.size())));
    testKeys.add(chunkStartAddresses.get(rand.nextInt(chunkStartAddresses.size())));
    LOG.info("Multiproof construction begins...");
    MultiMerkleProof multiMerkleProof = codeTrie.getValuesWithMultiMerkleProof(testKeys);
    Bytes32 codeTrieRootHash = codeTrie.getRootHash();
    Bytes32 computedRootHash = multiMerkleProof.computeRootHash();
    multiMerkleProof.printStats();
    LOG.info("Multiproof constructed. Trie Root Hash = {}, Computed Root hash = {}, Verified = {}",
      codeTrieRootHash.toHexString(), computedRootHash.toHexString(),
      codeTrieRootHash.equals(computedRootHash));
  }
}