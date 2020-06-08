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

import java.math.BigInteger;
import java.util.ArrayList;

import static org.apache.logging.log4j.LogManager.getLogger;

public class StrictFixedSizeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  private int threshold;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public StrictFixedSizeAnalysis(Bytes code, int threshold) {
    super(code);
    this.threshold = threshold;
  }

  public ArrayList<Integer> analyse() {
    int pc = 0;
    int currentChunkSize = 0;
    ArrayList<Integer> chunkStartOffsets = new ArrayList<>();
    chunkStartOffsets.add(0);

    LOG.info("Possible End of code: {}", this.possibleEndOfCode);
    while (pc != this.possibleEndOfCode) {

      final Operation curOp = registry.get(code.get(pc), 0);
      if (curOp == null) {
        LOG.error("Unknown opcode 0x{} at PC {}", Integer.toHexString(code.get(pc)), PcUtils.pcStr(pc));
        throw new Error("Unknown opcode");
      }
      int opSize = curOp.getOpSize();
      if (curOp.getOpcode() == InvalidOperation.OPCODE) {
        LOG.info("Invalid OPCODE is hit. Ending.");
        break;
      }

      if(currentChunkSize + opSize >= threshold) {
        currentChunkSize = 0;
        pc += opSize;
        // Since the start addresses are fairly standard, we will track the offset to the first
        // instruction in the chunk in this analysis.
        chunkStartOffsets.add(pc % threshold);
        continue;
      }

      currentChunkSize += opSize;
      pc += opSize;
    }

    return chunkStartOffsets;
  }
}