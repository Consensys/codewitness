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
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.Proof;
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.SimpleMerklePatriciaTrie;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;
import tech.pegasys.poc.witnesscodeanalysis.trie.ethereum.trie.MerklePatriciaTrie;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FixedSizeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  private int threshold;
  private boolean isInvalidSeen;
  private ArrayList<Integer> chunkStartAddresses;
  private MerklePatriciaTrie<Bytes32, Bytes> codeTrie;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public FixedSizeAnalysis(Bytes code, int threshold) {
    super(code);
    this.threshold = threshold;
    isInvalidSeen = false;
    chunkStartAddresses = null;
  }

  /*
   * This method creates chunks of the given code and populates the chunkStartAddresses with the
   * start addresses of the chunks.
   */
  public void createChunks() {
    int pc = 0;
    int currentChunkSize = 0;
    chunkStartAddresses = new ArrayList<>();
    chunkStartAddresses.add(0);

    while (pc != this.possibleEndOfCode) {

      final Operation curOp = registry.get(code.get(pc), 0);
      if (curOp == null) {
        LOG.error("Unknown opcode 0x{} at PC {}", Integer.toHexString(code.get(pc)), PcUtils.pcStr(pc));
        throw new Error("Unknown opcode");
      }
      int opSize = curOp.getOpSize();

      if(isInvalidSeen && curOp.getOpcode() == JumpOperation.OPCODE) {
        LOG.info("JUMP after Invalid is seen. Ending.");
        break;
      }

      if (curOp.getOpcode() == InvalidOperation.OPCODE) {
        LOG.info("Invalid OPCODE is hit.");
        isInvalidSeen = true;
      }

      if(currentChunkSize + opSize >= threshold) {
        currentChunkSize = 0;
        pc += opSize;
        chunkStartAddresses.add(pc);
        continue;
      }

      currentChunkSize += opSize;
      pc += opSize;
    }
    LOG.trace("  Finished. {} chunks", chunkStartAddresses.size());
  }

  /*
   * This method uses the chunk start addresses to create a SimpleMerklePatriciaTrie with
   * plain natural number addresses as keys, and code chunks as values
   */
  public void merkelize() {
    codeTrie = new SimpleMerklePatriciaTrie<Bytes32, Bytes>(v->v);
    int numChunks = chunkStartAddresses.size();
    // The keys are just plain natural number indices
    for (int index=0; index < numChunks; index++) {
      int thisChunkStart = chunkStartAddresses.get(index);

      int length = (index == numChunks - 1) ?
        code.size() - thisChunkStart :
        chunkStartAddresses.get(index+1) - thisChunkStart;

      Bytes chunk = this.code.slice(thisChunkStart, length);
      codeTrie.put(Bytes32.leftPad(Bytes.of(index)), chunk);
    }
    LOG.trace("Merkelization finished.");
  }

  /*
   * This method constructs proof and prints some statistics
   */
  public void proof(int index) {
    Proof<Bytes> proof = codeTrie.getValueWithProof(Bytes32.leftPad(Bytes.of(index)));
  }
}