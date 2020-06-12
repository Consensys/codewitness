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
import tech.pegasys.poc.witnesscodeanalysis.vm.Code;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodePaths {
  private static final Logger LOG = getLogger();

  // Entire contract.
  Bytes code;
  int codeSize;

  // Code segments for the start of the contract - up to the end of the function id block.
  // The key is the start offset within the code of the code segment.
  private Map<Integer, CodeSegment> functionBlockCodeSegments = new TreeMap<>();

  // List of functions identified in the code.
  // Key is the funciton id, value is the start offset of the code segment containing function id PUSH4 operation.
  private Map<Bytes, Integer> foundFunctions = new TreeMap<>();

  // Happy and sad path code segments for each function id.
  // The key to the outer map is the function id.
  // The key to the inner map is the start offset with the code of the code segment.
  private Map<Bytes, Map<Integer, CodeSegment>> allCodePaths = new TreeMap<>();

  // Map of function id to Map of start to length.
  private Map<Bytes, Map<Integer, Integer>> allCombinedCodeBlocks = new TreeMap<>();

  Set<Integer> jumpDests;

  public CodePaths(Bytes code, Set<Integer> jumpDests) {
    this.code = code;
    this.codeSize = code.size();
    this.jumpDests = jumpDests;
  }

  public void findFunctionBlockCodePaths(int endOfFunctionIdBlock) {

    final Deque<MessageFrame> messageFrameStack = new ArrayDeque<>();
    final ReturnStack returnStack = new ReturnStack(MessageFrame.DEFAULT_MAX_RETURN_STACK_SIZE);

    int maxStackSize = 1024;

    final MessageFrame frame =
        MessageFrame.builder()
            .messageFrameStack(messageFrameStack)
            .returnStack(returnStack)
            .code(new Code(this.code))
            .depth(0)
            .maxStackSize(maxStackSize)
            .build();

    messageFrameStack.addFirst(frame);
    frame.setState(MessageFrame.State.CODE_EXECUTING);

    CodeVisitor visitor = new CodeVisitor(this.code, this.functionBlockCodeSegments, this.foundFunctions, endOfFunctionIdBlock, this.jumpDests);
    visitor.visit(frame, 0);
  }


  public void findCodeSegmentsForFunctions() {
    LOG.trace("Find Code Paths functions");
    for (Bytes functionId: this.foundFunctions.keySet()) {
      LOG.trace("Find Code Paths for functionid: {}", functionId);
      int functionStartOp = this.foundFunctions.get(functionId);

      Map<Integer, CodeSegment> functionCodeSegments = new TreeMap<>();

      CodeSegment functionCallFromSegment = this.functionBlockCodeSegments.get(functionStartOp);
      functionCodeSegments.put(functionStartOp, functionCallFromSegment);

      CodeSegment currentSegment = functionCallFromSegment;

      // Go from the segment where the jumpi that goes to the function body towards the start of the code, PC=0.
      boolean foundStart = false;
      do {
        if (currentSegment.start == 0) {
          foundStart = true;
        }
        currentSegment = this.functionBlockCodeSegments.get(currentSegment.previousSegments.iterator().next());
        functionCodeSegments.put(currentSegment.start, currentSegment);
      } while (!foundStart);

      // Find all code segments that are reachable by the function.
      final Deque<MessageFrame> messageFrameStack = new ArrayDeque<>();
      final ReturnStack returnStack = new ReturnStack(MessageFrame.DEFAULT_MAX_RETURN_STACK_SIZE);
      int maxStackSize = 1024;
      final MessageFrame frame =
          MessageFrame.builder()
              .messageFrameStack(messageFrameStack)
              .returnStack(returnStack)
              .code(new Code(this.code))
              .depth(0)
              .maxStackSize(maxStackSize)
              .build();
      frame.setPC(functionCallFromSegment.nextSegmentJumps.iterator().next());
      messageFrameStack.addFirst(frame);
      frame.setState(MessageFrame.State.CODE_EXECUTING);

      CodeVisitor visitor = new CodeVisitor(this.code, functionCodeSegments, this.jumpDests);
      visitor.visit(frame, 0);

      this.allCodePaths.put(functionId, functionCodeSegments);
    }
  }

  public void showAllCodePaths() {
    LOG.trace("Show All Code Paths");
    for (Bytes functionId: this.allCodePaths.keySet()) {
      LOG.trace(" Code Paths for functionid: {}", functionId);
      Map<Integer, CodeSegment> functionCodeSegments = this.allCodePaths.get(functionId);

      for (CodeSegment seg : functionCodeSegments.values()) {
        LOG.trace("  {}", seg);
      }
    }
  }

  /**
   * Check that all of code is accessed
   *
   * @return
   */
  public boolean validateCodeSegments(int endOfCodeOffset) {
    LOG.trace("Validating Code Segments");
    boolean done = false;
    int pc = 0;

    boolean firstOpCodeNotInSegment = true;
    while (!done) {
      int next = CodeSegment.INVALID;
      StringBuffer functionsUsingSegment = new StringBuffer();
      for (Bytes functionId: this.allCodePaths.keySet()) {
        Map<Integer, CodeSegment> codeSegments = this.allCodePaths.get(functionId);
        CodeSegment codeSegment = codeSegments.get(pc);
        if (codeSegment != null) {
          if (functionsUsingSegment.length() != 0) {
            functionsUsingSegment.append(", ");
          }
          functionsUsingSegment.append(functionId);
          int proposedNext = codeSegment.length + pc;
          if (next != CodeSegment.INVALID) {
            if (next != proposedNext) {
              LOG.error("Next {} != Proposed Next: {}", next, proposedNext);
              throw new RuntimeException("Next doesn't match proposed next");
            }
          }
          else {
            next = proposedNext;
          }
        }
      }

      // None of the functions have a code segment at PC value next.
      if (next == CodeSegment.INVALID) {
        if (firstOpCodeNotInSegment) {
          firstOpCodeNotInSegment = false;
          logOpCode(pc);
        }
        else {
          byte opCode = this.code.get(pc);
          if (opCode == JumpDestOperation.OPCODE) {
            logOpCode(pc);
          }
          else {
            //logOpCode(pc);
          }
        }

        next = pc + getOpCodeLength(pc);
        if (next > endOfCodeOffset) {
          LOG.error("Reached end of code at offset 0x{} ({})", Integer.toHexString(this.codeSize), this.codeSize);
          // TODO say the format is correct to differentiate from the errors above.
          return true;
        }
      }
      else {
        LOG.trace("Code segment at offset: 0x{} ({}) used by functions: {}", Integer.toHexString(pc), pc, functionsUsingSegment);
        firstOpCodeNotInSegment = true;
      }
      pc = next;

      if (pc > endOfCodeOffset+1) {
        LOG.error("Gone past end of code: pc: {}, end of code: {}", pc, endOfCodeOffset);
        throw new RuntimeException("Gone past end of code");
      }
      if (pc == endOfCodeOffset || pc == endOfCodeOffset+1) {
        done = true;
      }
    }
    return true;
  }




  public void combineCodeSegments(int maxNumBytesBetweenCodeSegments) {
    for (Bytes functionId: this.allCodePaths.keySet()) {
//      LOG.info("Combining Code Segments for function: {}", functionId);

      Map<Integer, CodeSegment> allCodeSegments = this.allCodePaths.get(functionId);
      Map<Integer, Integer> combinedCodeSegments = new TreeMap<>();

      TreeSet<Integer> pathSet = new TreeSet<>(allCodeSegments.keySet());
      Iterator<Integer> iter = pathSet.iterator();

      int next = iter.next();
      if (next != 0) {
        throw new RuntimeException("Code didn't start at zero!");
      }
      int len = allCodeSegments.get(next).length;
      int startOfs = next;

      next = iter.next();
      do {
        if (next - maxNumBytesBetweenCodeSegments <= startOfs + len) {
          // Combine segments
          len = next - startOfs + allCodeSegments.get(next).length;
        } else {
          combinedCodeSegments.put(startOfs, len);
          startOfs = next;
          len = allCodeSegments.get(next).length;
        }
        next = iter.hasNext() ? iter.next() : CodeSegment.INVALID;
      } while (next != CodeSegment.INVALID);
      // Don't forget to do the final segment!
      combinedCodeSegments.put(startOfs, len);

      this.allCombinedCodeBlocks.put(functionId, combinedCodeSegments);

      LOG.trace("Combined CodeSegments: Function: {}, Prior to Combination: {}, Combined: {}",
        functionId, allCodeSegments.size(), combinedCodeSegments.size());
    }
  }

//  public void estimateWitnessSize() {
//    LOG.info("Contract size: {}", this.code.size());
//
//    for (Bytes functionId: this.allCombinedCodeSegments.keySet()) {
//      Map<Integer, CodeSegment> combinedCodeSegments = this.allCombinedCodeSegments.get(functionId);
//      int sizeOfCodeSegmentsIndicators = 4; // length = 2 bytes, start offset = 2 bytes.
//      int numCodeSegments = combinedCodeSegments.size();
//      int sizeOfAllCodeSegmentIndicators = numCodeSegments * sizeOfCodeSegmentsIndicators;
//      int sizeOfLengthFieldForCodeSegments = 2; // Assume there need to be up to 2**16 code segments.
//      int lenOfCode = 0;
//      for (Integer startOfs: combinedCodeSegments.keySet()) {
//        lenOfCode += combinedCodeSegments.get(startOfs).length;
//      }
//      int total = sizeOfAllCodeSegmentIndicators + sizeOfLengthFieldForCodeSegments + lenOfCode;
//      // Add in the message digest of the code used.
//      LOG.info("Estimated Witness Size for function: {} is: {} + {} + {} = {}",
//          functionId, sizeOfAllCodeSegmentIndicators, sizeOfLengthFieldForCodeSegments, lenOfCode, total);
//
//    }
//  }

  private void logOpCode(int offset) {
    LOG.trace("No code segment at offset: {}, opcode: {}", PcUtils.pcStr(offset), getOpCodeString(offset));
  }

  private String getOpCodeString(int offset) {
    byte opCodeValue = this.code.get(offset);
    Operation opCode = MainnetEvmRegistries.REGISTRY.get(opCodeValue, 0);
    if (opCode != null) {
      return opCode.getName();
    }
    else {
      return Integer.toHexString(opCodeValue);
    }
  }

  private int getOpCodeLength(int offset) {
    byte opCodeValue = this.code.get(offset);
    Operation opCode = MainnetEvmRegistries.REGISTRY.get(opCodeValue, 0);
    if (opCode != null) {
      return opCode.getOpSize();
    }
    else {
      return 1;
    }
  }

  public Map<Bytes, Map<Integer, Integer>> getAllCombinedCodeBlocks() {
    return allCombinedCodeBlocks;
  }
}
