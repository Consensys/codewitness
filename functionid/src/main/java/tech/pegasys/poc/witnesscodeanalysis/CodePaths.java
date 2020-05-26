package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.Code;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnStack;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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

  private Map<Bytes, Map<Integer, CodeSegment>> allCombinedCodeSegments = new TreeMap<>();


  public CodePaths(Bytes code) {
    this.code = code;
    this.codeSize = code.size();
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

    CodeVisitor visitor = new CodeVisitor(this.code, this.functionBlockCodeSegments, this.foundFunctions, endOfFunctionIdBlock);
    visitor.visit(frame, 0);
  }


  public void findCodeSegmentsForFunctions() {
    LOG.info("Find Code Paths functions");
    for (Bytes functionId: this.foundFunctions.keySet()) {
      LOG.info("Find Code Paths for functionid: {}", functionId);
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
        currentSegment = this.functionBlockCodeSegments.get(currentSegment.previousSegment);
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
      frame.setPC(functionCallFromSegment.nextSegmentJump);
      messageFrameStack.addFirst(frame);
      frame.setState(MessageFrame.State.CODE_EXECUTING);

      CodeVisitor visitor = new CodeVisitor(this.code, functionCodeSegments);
      visitor.visit(frame, 0);

      this.allCodePaths.put(functionId, functionCodeSegments);
    }
  }

  public void showAllCodePaths() {
    LOG.info("Show All Code Paths");
    for (Bytes functionId: this.allCodePaths.keySet()) {
      LOG.info(" Code Paths for functionid: {}", functionId);
      Map<Integer, CodeSegment> functionCodeSegments = this.allCodePaths.get(functionId);

      for (CodeSegment seg : functionCodeSegments.values()) {
        LOG.info("  {}", seg);
      }
    }
  }

  /**
   * Check that all of code is accessed
   *
   * @return
   */
  public boolean validateCodeSegments(int endOfCodeOffset) {
    LOG.info("Validating Code Segments");
    boolean done = false;
    int pc = 0;

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
        LOG.info("No code segment at offset: {}, opcode: {}", pc, getOpCodeString(pc));
        next = pc + getOpCodeLength(pc);
        if (next > endOfCodeOffset) {
          LOG.error("Reached end of code at offset {}", this.codeSize);
          // TODO say the format is correct to differentiate from the errors above.
          return true;
        }
      }
      else {
        LOG.info("Code segment at offset: {} used by functions: {}", pc, functionsUsingSegment);
      }
      pc = next;

      if (pc > endOfCodeOffset) {
        LOG.error("Gone past end of code: pc: {}, end of code: {}", pc, endOfCodeOffset);
        throw new RuntimeException("Gone past end of code");
      }
      if (pc == endOfCodeOffset) {
        done = true;
      }
    }
    return true;



//    CodeSegment codeSegment = this.functionBlockCodeSegments.get(0);
//    Set<Integer> futureCodeSegmentStart = new HashSet<>();
//    while (true) {
//      int nextSeg = codeSegment.start + codeSegment.length;
//      LOG.info(" Start: {}, Next: {}", codeSegment.start, nextSeg);
//      futureCodeSegmentStart.remove(nextSeg);
//
//      if (codeSegment.nextSegmentJump > nextSeg) {
//        futureCodeSegmentStart.add(codeSegment.nextSegmentJump);
//      }
//      else {
//        if (codeSegment.nextSegmentJump != CodeSegment.INVALID && this.functionBlockCodeSegments.get(codeSegment.nextSegmentJump) == null) {
//          LOG.error("Jump destination invalid for code segment: {}", codeSegment);
//          throw new RuntimeException("Jump destination invalid");
//        }
//      }
//      int lastOpCodeOffset = endOfCodeOffset-1;
//      if (nextSeg == lastOpCodeOffset) {
//        if (this.code.get(lastOpCodeOffset) == (byte)InvalidOperation.OPCODE) {
//          if (futureCodeSegmentStart.isEmpty()) {
//            return true;
//          }
//          LOG.info("Contained unused nextSegmentJumps: {}", futureCodeSegmentStart.size());
//          return false;
//        }
//        LOG.info("Code correct length, but ends in opcode: {}", MainnetEvmRegistries.REGISTRY.get(this.code.get(lastOpCodeOffset), 0).getName());
//        return false;
//      }
//      else {
//        codeSegment = this.functionBlockCodeSegments.get(nextSeg);
//        if (codeSegment == null) {
//          int search = nextSeg + 1;
//          while (codeSegment == null) {
//            codeSegment = this.functionBlockCodeSegments.get(search++);
//            if (search > this.codeSize) {
//              LOG.error("Reached end of code at offset {}", this.codeSize);
//              // TODO say the format is correct to differentiate from the errors above.
//              return true;
//            }
//          }
//          int nextSegStarts = search - 2;
//
//          LOG.info(" *****Can't find segment for {}, length: {}. opCode({}) is {}, and opcode({}) is {}",
//              nextSeg, nextSegStarts - nextSeg,
//              nextSeg, getOpCodeString(nextSeg),
//              nextSegStarts, getOpCodeString(nextSegStarts));
////          nextSeg = nextSegStarts;
//        }
//      }
  }




  public void combineCodeSegments(int maxNumBytesBetweenCodeSegments) {
    for (Bytes functionId: this.allCodePaths.keySet()) {
//      LOG.info("Combining Code Segments for function: {}", functionId);

      Map<Integer, CodeSegment> happyPathCodeSegments = this.allCodePaths.get(functionId);
      Map<Integer, CodeSegment> combinedCodeSegments = new TreeMap<>();

      TreeSet<Integer> happyPathSet = new TreeSet<>();
      happyPathSet.addAll(happyPathCodeSegments.keySet());
      Iterator<Integer> happyIter = happyPathSet.iterator();

      int nextHappy = happyIter.next();
      if (nextHappy != 0) {
        throw new RuntimeException("Code didn't start at zero!");
      }
      CodeSegment newSegment = new CodeSegment(nextHappy, CodeSegment.INVALID);
      int startOfs = nextHappy;
      int len = happyPathCodeSegments.get(nextHappy).length;


      nextHappy = happyIter.next();
      do {
        if (nextHappy - maxNumBytesBetweenCodeSegments <= startOfs + len) {
          // Combine segments
          len = nextHappy - startOfs + happyPathCodeSegments.get(nextHappy).length;
        } else {
          newSegment.setValuesLengthOnly(len);
          combinedCodeSegments.put(startOfs, newSegment);
          startOfs = nextHappy;
          len = happyPathCodeSegments.get(nextHappy).length;
          newSegment = new CodeSegment(startOfs, CodeSegment.INVALID);
        }
        nextHappy = happyIter.hasNext() ? happyIter.next() : CodeSegment.INVALID;
    } while (nextHappy != CodeSegment.INVALID);
    // Don't forget to do the final segment!
    newSegment.setValuesLengthOnly(len);
    combinedCodeSegments.put(startOfs, newSegment);

    this.allCombinedCodeSegments.put(functionId, combinedCodeSegments);

    LOG.info("Combined CodeSegments: Function: {}, Prior to Combination: {}, Combined: {}",
        functionId, happyPathCodeSegments.size(), combinedCodeSegments.size());
  }
  }

  public void showCombinedCodeSegments() {
    for (Bytes functionId : this.allCombinedCodeSegments.keySet()) {
      Map<Integer, CodeSegment> combinedCodeSegments = this.allCombinedCodeSegments.get(functionId);
      for (Integer startOfs : combinedCodeSegments.keySet()) {
        LOG.info(" Function {}, Start: {}, Length: {}", functionId, startOfs, combinedCodeSegments.get(startOfs).length);
      }
    }
  }

  public void estimateWitnessSize() {
    LOG.info("Contract size: {}", this.code.size());

    for (Bytes functionId: this.allCombinedCodeSegments.keySet()) {
      Map<Integer, CodeSegment> combinedCodeSegments = this.allCombinedCodeSegments.get(functionId);
      int sizeOfCodeSegmentsIndicators = 4; // length = 2 bytes, start offset = 2 bytes.
      int numCodeSegments = combinedCodeSegments.size();
      int sizeOfAllCodeSegmentIndicators = numCodeSegments * sizeOfCodeSegmentsIndicators;
      int sizeOfLengthFieldForCodeSegments = 2; // Assume there need to be up to 2**16 code segments.
      int lenOfCode = 0;
      for (Integer startOfs: combinedCodeSegments.keySet()) {
        lenOfCode += combinedCodeSegments.get(startOfs).length;
      }
      int total = sizeOfAllCodeSegmentIndicators + sizeOfLengthFieldForCodeSegments + lenOfCode;
      // Add in the message digest of the code used.
      LOG.info("Estimated Witness Size for function: {} is: {} + {} + {} = {}",
          functionId, sizeOfAllCodeSegmentIndicators, sizeOfLengthFieldForCodeSegments, lenOfCode, total);

    }
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

}
