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
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PushOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.RevertOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.StopOperation;

import java.util.HashSet;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

public class SimpleAnalysis {
  private static final Logger LOG = getLogger();

  private static final int START_LEN = 5;
  private static final Bytes OLD_SOL_START = Bytes.fromHexString("0x6060604052");
  private static final Bytes NEW_SOL_START = Bytes.fromHexString("0x6080604052");


  private Bytes code;
  private boolean isProbablySolidity;
  private boolean isNewSolidity;
  private int endOfFunctionIdBlock = -1;
  private int endOfCode;
  private boolean endOfCodeDetected = false;
  private Set<Integer> jumpDests;

  public SimpleAnalysis(Bytes code) {
    this.code = code;
    //LOG.trace(" Contract bytecode: {}", this.code);
    checkIfSolidity();

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

  public boolean isNewSolidity() {
    return isNewSolidity;
  }

  public int getEndOfFunctionIdBlock() {
    return endOfFunctionIdBlock;
  }

  public int getEndOfCode() {
    return endOfCode;
  }

  public boolean endOfCodeDetected() {
    return endOfCodeDetected;
  }

  public Set<Integer> getJumpDests() {
    return jumpDests;
  }

  private void checkIfSolidity() {
    int len = this.code.size();
    if (len < START_LEN) {
      return;
    }

    Bytes codeStart = this.code.slice(0, START_LEN);
    if (codeStart.compareTo(NEW_SOL_START) == 0) {
      LOG.trace("New Solidity start of code detected");
      this.isProbablySolidity = true;
      this.isNewSolidity = true;
    }
    else if (codeStart.compareTo(OLD_SOL_START) == 0) {
      LOG.trace("Old Solidity start of code detected");
      this.isProbablySolidity = true;
    }
    else {
      LOG.trace(" Not Solidity contract starts with: {}", codeStart);
    }
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
      if (curOp.getOpcode() == CallDataLoadOperation.OPCODE) {
        done = true;
      }
      pc = pc + curOp.getOpSize();
      if (pc >= this.endOfCode) {
        LOG.trace("No CALLDATALOAD found in code: analysis unlikely to work");
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
      if ((curOp.getOpcode() == RevertOperation.OPCODE) ||
          (curOp.getOpcode() == StopOperation.OPCODE) ||
          (curOp.getOpcode() == ReturnOperation.OPCODE)) {
        done = true;
      } else {
        pc = pc + curOp.getOpSize();
        if (pc >= this.endOfCode) {
          LOG.trace("No REVERT, STOP, or RETURN found in code: Code analysis for apparently Solidity file is unlikely to work properly");
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
        if (pc >= this.endOfCode) {
          LOG.trace("No JUMP or RETURN or STOP followed by INVALID operation found in code");
          return;
        }
      }
    }
    this.endOfCode = pc;

    this.endOfCodeDetected = true;
  }

  private void findJumpDests() {
    int pc = 0;
    this.jumpDests = new HashSet<>();

    while (pc < this.endOfCode) {
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
