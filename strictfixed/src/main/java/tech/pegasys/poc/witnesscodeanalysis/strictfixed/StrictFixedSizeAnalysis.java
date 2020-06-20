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
package tech.pegasys.poc.witnesscodeanalysis.strictfixed;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.CodeAnalysisBase;
import tech.pegasys.poc.witnesscodeanalysis.common.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.apache.logging.log4j.LogManager.getLogger;

public class StrictFixedSizeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  private int chunkSize;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public StrictFixedSizeAnalysis(Bytes code, int chunkSize) {
    super(code);
    this.chunkSize = chunkSize;
  }

  public ArrayList<Integer> analyse() {
    int pc = 0;
    int currentChunkSize = 0;
    ArrayList<Integer> chunkStartOffsets = new ArrayList<>();
    chunkStartOffsets.add(0);

    int codeLength = this.code.size();

    // True when the part of the contract being processed is definitely code.
    boolean executableCodeSection = true;

    LOG.trace(" Contract size: {}", codeLength);
    while (pc < codeLength) {
      if (executableCodeSection) {
        // While processing executable code, determine the start offset of the
        // the first opcode.
        final Operation curOp = registry.get(code.get(pc), 0);
        if (curOp == null) {
          LOG.trace(" Found unknown opcode at PC: {}", PcUtils.pcStr(pc));
          executableCodeSection = false;
          // Move the PC to the end of the chunk.
          pc += this.chunkSize - pc % this.chunkSize;
          continue;
        }
        int opSize = curOp.getOpSize();

        // Detect the end of the chunk
        if(currentChunkSize + opSize >= this.chunkSize) {
          currentChunkSize = currentChunkSize + opSize - this.chunkSize;
          pc += opSize;
          // Since the start addresses are fairly standard, we will track the offset to the first
          // instruction in the chunk in this analysis.
          chunkStartOffsets.add(currentChunkSize);
        }
        else {
          currentChunkSize += opSize;
          pc += opSize;
        }
      }
      else {
        // processing non-executable code
        chunkStartOffsets.add(0);
        pc += this.chunkSize;
      }
    }

    return chunkStartOffsets;
  }
}