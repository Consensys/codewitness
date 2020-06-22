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
package tech.pegasys.poc.witnesscodeanalysis.jumpdest;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.CodeAnalysisBase;
import tech.pegasys.poc.witnesscodeanalysis.common.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;

import java.math.BigInteger;
import java.util.ArrayList;


import static org.apache.logging.log4j.LogManager.getLogger;

public class JumpDestAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  int threshold;
  private boolean isInvalidSeen;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public JumpDestAnalysis(Bytes code, int threshold) {
    super(code);
    this.threshold = threshold;
    isInvalidSeen = false;
  }

  public ArrayList<Integer> analyse() {
    int pc = 0;
    int currentChunkSize = 0;
    ArrayList<Integer> chunkStartAddresses = new ArrayList<>();
    chunkStartAddresses.add(0);

    int codeLength = this.code.size();

    // True when the part of the contract being processed is definitely code.
    boolean executableCodeSection = true;

    while (pc < codeLength) {
      if (executableCodeSection) {
        final Operation curOp = registry.get(code.get(pc), 0);
        if (curOp == null) {
          LOG.trace("  Found unknown opcode at PC: {}", PcUtils.pcStr(pc));
          executableCodeSection = false;
          // Move the PC to the end of the chunk.
          pc += this.threshold - pc % this.threshold;
          continue;
        }
        int opSize = curOp.getOpSize();
        int opCode = curOp.getOpcode();

        if (opCode == JumpDestOperation.OPCODE) {
          LOG.trace("  Found JumpDest at {}", PcUtils.pcStr(pc));

          if(currentChunkSize + opSize >= this.threshold) {
            currentChunkSize = 0;
            pc += opSize;
            chunkStartAddresses.add(pc);
            continue;
          }
        }
        currentChunkSize += opSize;
        pc += opSize;
      }
      else {
        // processing non-executable code
        chunkStartAddresses.add(pc);
        pc += this.threshold;
      }
    }
    LOG.trace(" JumpDest Analysis found chunk starting addresses: ");
    for(Integer e : chunkStartAddresses) {
      LOG.trace(PcUtils.pcStr(e));
    }

    return chunkStartAddresses;
  }
}
