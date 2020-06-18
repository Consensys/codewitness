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
package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcess;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperandStack;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.DupOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.EqOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpiOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PushOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.RevertOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.SelfDestructOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.StopOperation;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;
import static tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation.DYNAMIC_MARKER;
import static tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation.DYNAMIC_MARKER_MASK;


/**
 * Code Visitor can be used in two modes:
 * - Find Functions Mode: find the functions that exist in the code, the code segments they are in, and all
 *  code segments from teh start of code to the functions.
 * - Reachable code mode: find all code that is reachable from a function entry point.
 */
public class CodeVisitor {
  private static final Logger LOG = getLogger();

  private Bytes code;
  public Map<Integer, CodeSegment> codeSegments;
  private Map<Bytes, Integer> foundFunctions;
  private int pcEndOfFunctionBlock;
  private boolean findFunctionsMode;
  private Set<Integer> jumpDests;


  /**
   * Constructor used for finding the list of functions that exist in the code, the code segments they are in, and all
   * code segments from the start of code to the functions.
   */
  public CodeVisitor(Bytes code, Map<Integer, CodeSegment> codeSegments, Map<Bytes, Integer> foundFunctions, int pcEndOfFunctionBlock, Set<Integer> jumpDests) {
    this.code = code;
    this.codeSegments = codeSegments;
    this.foundFunctions = foundFunctions;
    this.pcEndOfFunctionBlock = pcEndOfFunctionBlock;
    this.findFunctionsMode = true;
    this.jumpDests = jumpDests;
  }

  /**
   * Constructor to use for reachable code mode.
   */
  public CodeVisitor(Bytes code, Map<Integer, CodeSegment> codeSegments, Set<Integer> jumpDests) {
    this.code = code;
    this.codeSegments = codeSegments;
    this.findFunctionsMode = false;
    this.jumpDests = jumpDests;
  }


  public void visit(MessageFrame frame, int callingSegmentPc) {
    int pc = frame.getPC();
    int startingPc = pc;
    addCodeSegment(startingPc, callingSegmentPc, frame);
    CodeSegment codeSegment = this.codeSegments.get(startingPc);
    boolean done = false;

    while (!done) {
//      if (isInStack(frame, 0)) {
//        LOG.info("in stack");
//      }
//      else {
//        LOG.info("not in stack");
//      }

      final Operation curOp = MainnetEvmRegistries.REGISTRY.get(code.get(pc), 0);
      int opCode = curOp.getOpcode();
      frame.setCurrentOperation(curOp);
      int jumpDest = curOp.execute(frame).intValue();

      if (this.findFunctionsMode) {
        findFunctionStateMachine(frame, startingPc, opCode);
      }

      // Process jumps.
      if (opCode == JumpiOperation.OPCODE || opCode == JumpOperation.OPCODE) {
        LOG.trace("PC: {}, Operation {}, Jump Destination: {}", PcUtils.pcStr(pc), curOp.getName(), PcUtils.pcStr(jumpDest));
        dumpStack(frame);

        if (jumpDest == startingPc) {
          // This is a looping construct, jumping back to the start of the segment
          LOG.trace("Skipping jump as Jump Dest == Starting PC for {}", PcUtils.pcStr(jumpDest));
        }
        else if ((jumpDest & DYNAMIC_MARKER_MASK) == DYNAMIC_MARKER) {
          int opCodeCausingJumpDest = jumpDest & 0xff;
          Operation op = MainnetEvmRegistries.REGISTRY.get(opCodeCausingJumpDest, 0);
          String message = "Jump created by opcode: " + op.getName() + " at PC: " + PcUtils.pcStr(pc);
          UnableToProcess.getInstance().unableToProcess(UnableToProcessReason.DYNAMIC_JUMP, message);
        }
        else if (!this.jumpDests.contains(jumpDest)) {
          String message = "JumpDest: " + PcUtils.pcStr(jumpDest) + " at PC: " + PcUtils.pcStr(pc);
          UnableToProcess.getInstance().unableToProcess(UnableToProcessReason.INVALID_JUMP_DEST, message);
        }
        else if (this.findFunctionsMode && (jumpDest > this.pcEndOfFunctionBlock)) {
          LOG.trace("Find Function Mode: Ignoring jump");
        }
        else {
          if (!beenHereBeforeWithSameStack(jumpDest, frame)) {
            // Not visited yet.
            MessageFrame newMessageFrame = (MessageFrame) frame.clone();
            newMessageFrame.setPC(jumpDest);
            visit(newMessageFrame, startingPc);
          } else {
            LOG.trace("Skipping jump as I have been to {} before with the same stack!", PcUtils.pcStr(jumpDest));
          }
        }
      }
      else {
        LOG.trace("PC: {}, Operation {}", PcUtils.pcStr(pc), curOp.getName());
        dumpStack(frame);
      }

      final int opSize = curOp.getOpSize();
      pc += opSize;
      frame.setPC(pc);

      // Process end of code segments.
      switch (opCode) {
        case JumpiOperation.OPCODE:
          // Start a new code segment after each JUMPI operation.
          codeSegment.setValuesJumpi(pc - startingPc, jumpDest, opCode);
          callingSegmentPc = startingPc;
          startingPc = pc;
          if (beenHereBeforeWithSameStack(startingPc, frame)) {
            LOG.trace("**Falling through to existing segment: {}", startingPc);
            return;
          }
          addCodeSegment(startingPc, callingSegmentPc, frame);
          codeSegment = this.codeSegments.get(startingPc);
          break;
        case JumpDestOperation.OPCODE:
          // End code segments immediately before JUMPDEST operations, if
          // the code has fallen through rather than jumped here.
          if (pc - opSize != startingPc) {
            codeSegment.setValuesJumpDest(pc - startingPc - opSize, opCode);
            callingSegmentPc = startingPc;
            startingPc = pc - opSize;
            if (beenHereBeforeWithSameStack(startingPc, frame)) {
              LOG.trace("**Falling through to existing segment: {}", startingPc);
              return;
            }
            addCodeSegment(startingPc, callingSegmentPc, frame);
            codeSegment = this.codeSegments.get(startingPc);
          }
          break;
        case JumpOperation.OPCODE:
        case JumpSubOperation.OPCODE:
          done = true;
          codeSegment.setValuesJump(pc - startingPc, jumpDest, opCode);
          break;
        case ReturnSubOperation.OPCODE:
          // TODO need to add in return logic.
          done = true;
          codeSegment.setValuesReturnSub(pc - startingPc, opCode);
          break;
        case ReturnOperation.OPCODE:
        case SelfDestructOperation.OPCODE:
        case StopOperation.OPCODE:
          done = true;
          codeSegment.setValuesHappyEnding(pc - startingPc, opCode);
          break;
        case InvalidOperation.OPCODE:
        case RevertOperation.OPCODE:
          done = true;
          codeSegment.setValuesSadEnding(pc - startingPc, opCode);
          break;
      }
    }
  }

  private boolean foundPush4OpCode = false;
  private boolean foundEqOpCode = false;
  private boolean foundPushDestAddress = false;
  private Bytes functionId = null;

  /**
   * Find function entry points. Look for code matching the pattern shown below:
   * PUSH4 0x95CACBE0
   * EQ
   * PUSH1 0x41
   * JUMPI
   *
   * @param frame
   * @param startingPc
   * @param opCode
   */
  private void findFunctionStateMachine(MessageFrame frame, int startingPc, int opCode) {
    if (this.foundPushDestAddress) {
      this.foundPushDestAddress = false;
      if (opCode == JumpiOperation.OPCODE) {
        LOG.trace("****Found function {} in code segment {}", this.functionId, startingPc);
        this.foundFunctions.put(this.functionId, startingPc);
      }
    }
    if (this.foundEqOpCode) {
      this.foundEqOpCode = false;
      if (opCode == PushOperation.PUSH1_OPCODE || opCode == PushOperation.PUSH2_OPCODE) {
        this.foundPushDestAddress = true;
      }
    }
    if (this.foundPush4OpCode) {
      this.foundPush4OpCode = false;
      if (opCode == EqOperation.OPCODE) {
        this.foundEqOpCode = true;
      }
      else if (DupOperation.isADupOpCode(opCode)) {
        // Some times there is a Dup2 between the PUSH4 and the EQ.
        // Stay in the "waiting for EQ opcode state"
        this.foundPush4OpCode = true;
      }
    }
    if (opCode == PushOperation.PUSH4_OPCODE) {
      this.functionId = frame.getStackItem(0).slice(28, 4);
      this.foundPush4OpCode = true;
    }
  }


  private void addCodeSegment(int startingPc, int callingSegmentPc, MessageFrame frame) {
//    if (beenHereBeforeWithSameStack(startingPc, frame)) {
//      LOG.error("Been here before with the same stack for start PC {}", startingPc);
//      return;
//    }
    CodeSegment codeSegment = this.codeSegments.get(startingPc);
    if (codeSegment != null) {
      codeSegment.addNewPrevious(callingSegmentPc, frame.getCopyOfStack());
    }
    else {
      codeSegment = new CodeSegment(startingPc, callingSegmentPc, frame.getCopyOfStack());
      this.codeSegments.put(startingPc, codeSegment);
    }
  }

  /**
   * Is the stack the same as another time the code at this startingPc has been called?
   *
   */
  private boolean beenHereBeforeWithSameStack(int startingPc, MessageFrame frame) {
    CodeSegment existingCodeSegment = this.codeSegments.get(startingPc);
    if (existingCodeSegment == null) {
      return false;
    }

    OperandStack stack = frame.getCopyOfStack();

    // Try to detect recursive functions and deal with them separately.
    final int RECURSIVE_FUNCTION_THRESHOLD = 10;
    if (stack.size() > RECURSIVE_FUNCTION_THRESHOLD) {
      for (OperandStack existingStack: existingCodeSegment.previousSegmentStacks) {
        int stackToCheck = RECURSIVE_FUNCTION_THRESHOLD;
        if (existingStack.size() < stackToCheck) {
          stackToCheck = existingStack.size();
        }

        boolean matchingStackFound = true;
        for (int i=0; i < stackToCheck; i++) {
          if (stack.get(i).compareTo(existingStack.get(i)) != 0) {
            matchingStackFound = false;
            break;
          }
        }
        if (matchingStackFound) {
          return true;
        }
      }
      return false;


    }

    // else non-recursive function
    for (OperandStack existingStack: existingCodeSegment.previousSegmentStacks) {
      if (stack.size() != existingStack.size()) {
        continue;
      }
      boolean matchingStackFound = true;
      for (int i=0; i < stack.size(); i++) {
        if (stack.get(i).compareTo(existingStack.get(i)) != 0) {
          matchingStackFound = false;
          break;
        }
      }
      if (matchingStackFound) {
        return true;
      }
    }
    return false;
  }

  private void dumpStack(MessageFrame frame) {
    StringBuffer buf = new StringBuffer();
    buf.append(" Stack:");
    int stackSize = frame.stackSize();
    for (int i = 0; i < stackSize; i++) {
      buf.append(" [");
      buf.append(i);
      buf.append("]: ");
      buf.append(frame.getStackItem(i));
      buf.append(", ");
    }
    LOG.trace(buf.toString());
  }

  private boolean isInStack(MessageFrame frame, long val) {
    BigInteger searchVal = BigInteger.valueOf(val);
    for (int i = 0; i < frame.stackSize(); i++) {
      BigInteger stackItem = frame.getStackItem(i).toBigInteger();
      if (stackItem.compareTo(searchVal) == 0) {
        return true;
      }
    }
    return false;
  }

}
