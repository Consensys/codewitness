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
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CallDataLoadOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MStoreOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PushOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.RevertOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.StopOperation;

import java.util.HashSet;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

public class SimpleAnalysis {
  private static final Logger LOG = getLogger();

  private Bytes code;
  private boolean isProbablySolidity;
  private int endOfFunctionIdBlock = -1;
  private int endOfCode;
  private int startOfAuxData;
  private boolean simpleAnalysisCompleted = false;
  private Set<Integer> jumpDests;

  public SimpleAnalysis(Bytes code, int startOfAuxData) {
    this.code = code;
    this.startOfAuxData = startOfAuxData;
    this.isProbablySolidity = probablySolidity();
    this.endOfCode = code.size() - 1;
    findJumpDests();
    if (this.isProbablySolidity) {
      scanCode();
    }
  }


  public Set<Bytes> determineFunctionIds(int probableEndOfCode) {
    Set<Bytes> functionIds = new HashSet<>();

    int pc = 0;

    // Go until the call data is loaded.
    boolean done = false;
    while (!done) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Not a valid byte code value / operation.
        // Don't try to process any further.
        return functionIds;
      }
      if (curOp.getOpcode() == CallDataLoadOperation.OPCODE) {
        done = true;
      }
      pc = pc + curOp.getOpSize();
      if (pc > probableEndOfCode) {
        throw new Error("No REVERT found in code");
      }
    }

    // The next section contains the function ids. Keep going until the revert is encountered
    done = false;
    while (!done) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Not a valid byte code value / operation.
        // Don't try to process any further.
        return functionIds;
      }
      if (curOp.getOpcode() == RevertOperation.OPCODE) {
        done = true;
      } else if (curOp.getOpcode() == PushOperation.PUSH4_OPCODE) {
        Bytes functionId = code.slice(pc + 1, 4);
        functionIds.add(functionId);
      }
      pc = pc + curOp.getOpSize();
      if (pc > probableEndOfCode) {
        throw new Error("No REVERT found in code");
      }
    }
    return functionIds;
  }

  public boolean isProbablySolidity() {
    return isProbablySolidity;
  }

  public int getEndOfFunctionIdBlock() {
    return endOfFunctionIdBlock;
  }

  public int getEndOfCode() {
    return endOfCode;
  }

  public boolean simpleAnalysisCompleted() {
    return simpleAnalysisCompleted;
  }

  public Set<Integer> getJumpDests() {
    return jumpDests;
  }

  private boolean probablySolidity() {
    int len = code.size();
    if (len < 10) {
      return false;
    }

    // Look for:
    //    PUSH1 0x80
    //    PUSH1 0x40
    //    MSTORE
    return
        ((code.get(0) == (byte) PushOperation.PUSH1_OPCODE) &&
            (code.get(1) == (byte) 0x80) &&
            (code.get(2) == (byte) PushOperation.PUSH1_OPCODE) &&
            (code.get(3) == (byte) 0x40) &&
            (code.get(4) == (byte) MStoreOperation.OPCODE));
  }


  /**
   * Determine the PC of the REVERT at the end of the set of code segments that contain the
   * function ids.
   */
  private void scanCode() {
    int pc = 0;

    // Go until the call data is loaded.
    boolean done = false;
    while (!done) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Unknown opcode.
        return;
      }
      if (curOp.getName().equalsIgnoreCase("CALLDATALOAD")) {
        done = true;
      }
      pc = pc + curOp.getOpSize();
      if (pc >= this.startOfAuxData) {
        LOG.error("No CALLDATALOAD found in code: analysis unlikely to work");
        return;
      }
    }

    // The next section contains the function ids. Keep going until the revert is encountered
    done = false;
    while (!done) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Unknown opcode.
        return;
      }
      if (curOp.getOpcode() == RevertOperation.OPCODE) {
        done = true;
      } else {
        pc = pc + curOp.getOpSize();
        if (pc >= this.startOfAuxData) {
          LOG.error("No REVERT found in code: Code analysis for apparently Solidty file is unlikely to work properly");
          LOG.error(this.code);
          return;
        }
      }
    }
    this.endOfFunctionIdBlock = pc;

    // Now search for the Invalid opcode, indicating the end of the code.
    done = false;
    boolean nextCouldBeEnd = false;
    while (!done) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Unknown opcode.
        return;
      }
      switch (curOp.getOpcode()) {
        case JumpOperation.OPCODE:
        case ReturnOperation.OPCODE:
        case StopOperation.OPCODE:
          nextCouldBeEnd = true;
          break;
        case InvalidOperation.OPCODE:
          if (nextCouldBeEnd) {
            done = true;
          }
          break;
        default:
          nextCouldBeEnd = false;
          break;
      }

      if (!done) {
        pc = pc + curOp.getOpSize();
        if (pc >= this.startOfAuxData) {
          LOG.trace("No JUMP or RETURN or STOP followed by INVALID operation found in code");
          return;
        }
      }
    }
    this.endOfCode = pc;

    this.simpleAnalysisCompleted = true;
  }

  private void findJumpDests() {
    int pc = 0;
    this.jumpDests = new HashSet<>();

    while (pc < this.startOfAuxData) {
      Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      if (curOp == null) {
        // Unknown opcode.
        break;
      }
      if (curOp.getOpcode() == JumpDestOperation.OPCODE) {
        this.jumpDests.add(pc);
      }
      pc = pc + curOp.getOpSize();
    }
  }
}
