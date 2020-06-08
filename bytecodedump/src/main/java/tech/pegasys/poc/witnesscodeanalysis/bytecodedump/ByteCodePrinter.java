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
package tech.pegasys.poc.witnesscodeanalysis.bytecodedump;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;

import static java.lang.Math.min;
import static org.apache.logging.log4j.LogManager.getLogger;

public class ByteCodePrinter {
  private static final Logger LOG = getLogger();
  Bytes code;

  public ByteCodePrinter(Bytes code) {
    this.code = code;
  }

  public void print(int start, int end) {
    int pc = start;
    boolean done = false;
    while (!done) {
      byte opCodeValue = this.code.get(pc);
      Operation opCode = MainnetEvmRegistries.REGISTRY.get(opCodeValue, 0);
      int operationLength;
      String opCodeName;
      if (opCode != null) {
        operationLength = opCode.getOpSize();
        opCodeName = opCode.getName();
      }
      else {
        // Unknown opcode.
        operationLength = 1;
        opCodeName = Integer.toHexString(opCodeValue);
      }

      if (operationLength == 1) {
        LOG.info(" PC: 0x{}, opcode: {}", Integer.toHexString(pc), opCodeName);
      }
      else {
        int paramLength = operationLength - 1;
        final int copyLength = min(paramLength, code.size() - pc - 1);
        Bytes param = code.slice(pc + 1, copyLength);
        LOG.info(" PC: 0x{}, opcode: {} {}", Integer.toHexString(pc), opCodeName, param);
      }
      pc += operationLength;
      if (pc == end) {
        done = true;
      }
      if (pc > end) {
        LOG.error(" PC {} is past end {}", pc, end);
        done = true;
      }
    }
  }
}
