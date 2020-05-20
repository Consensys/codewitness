package tech.pegasys.poc.witnesscodeanalysis;

import com.sun.org.apache.xpath.internal.compiler.OpCodes;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.Code;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnStack;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodePaths {
  private static final Logger LOG = getLogger();

  // Entire contract.
  Bytes code;
  int codeSize;

  // Code segments for the entire contract.
  // The key is the start offset within the code of the code segment.
  Map<Integer, CodeSegment> codeSegments = new TreeMap<>();

  // List of functions identified in the code.
  Map<Bytes, Integer> foundFunctions = new TreeMap<>();

  // Happy and sad path code segments for each function id.
  // The key to the outer map is the function id.
  // The key to the inner map is the start offset with the code of the code segment.
  Map<Bytes, Map<Integer, CodeSegment>> allHappyPathCodeSegments = new TreeMap<>();
  Map<Bytes, Map<Integer, CodeSegment>> allSadPathCodeSegments = new TreeMap<>();


  Map<Bytes, Map<Integer, CodeSegment>> allCombinedCodeSegments = new TreeMap<>();


  public CodePaths(Bytes code) {
    this.code = code;
    this.codeSize = code.size();
  }

  public void findAllCodePaths() {
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

    CodeVisitor visitor = new CodeVisitor(this.code, this.codeSegments, this.foundFunctions);
    visitor.visit(frame, 0);
  }


  public void findCodeSegmentsForFunction(Bytes functionId, int functionStartOp) {
    LOG.info("Finding Happy and Sad Paths for functionid: {}", functionId);

    Map<Integer, CodeSegment> happyPathCodeSegments = new TreeMap<>();
    Map<Integer, CodeSegment> sadPathCodeSegments = new TreeMap<>();

    CodeSegment functionCallFromSegment = this.codeSegments.get(functionStartOp);
    happyPathCodeSegments.put(functionStartOp, functionCallFromSegment);

    CodeSegment currentSegment = functionCallFromSegment;

    // Go from the segment where the jumpi that goes to the function body towards the start of the code, PC=0.
    boolean foundStart = false;
    do {
      if (currentSegment.start == 0) {
        foundStart = true;
      }
      currentSegment = this.codeSegments.get(currentSegment.previousSegment);
      happyPathCodeSegments.put(currentSegment.start, currentSegment);
    } while (!foundStart);

    // Go from the segment where the jumpi that goes to the function body towards all of the termination points.
    CodeSegment segment = this.codeSegments.get(functionStartOp);
    findPaths(segment.nextSegmentJump, happyPathCodeSegments, sadPathCodeSegments);

    this.allHappyPathCodeSegments.put(functionId, happyPathCodeSegments);
    this.allSadPathCodeSegments.put(functionId, sadPathCodeSegments);

    LOG.info("Happy Path Code Segments");
    for (CodeSegment seg: happyPathCodeSegments.values()) {
      LOG.info(seg);
    }

    LOG.info("Sad Path Code Segments");
    for (CodeSegment seg: sadPathCodeSegments.values()) {
      LOG.info(seg);
    }

  }


  private void findPaths(int functionStartOp,
                 Map<Integer, CodeSegment> happyPathCodeSegments, Map<Integer, CodeSegment> sadPathCodeSegments) {
    CodeSegment currentSegment = this.codeSegments.get(functionStartOp);
    if (currentSegment == null) {
      LOG.error("No code segment found for(1) {}", functionStartOp);
      throw new RuntimeException("No code segment found");
    }

    boolean foundEnd = false;
    do {
      if (currentSegment.endsProgram) {
        if (currentSegment.happyPathEnding) {
          happyPathCodeSegments.put(currentSegment.start, currentSegment);
        }
        else {
          sadPathCodeSegments.put(currentSegment.start, currentSegment);
        }
      }
      else {
        happyPathCodeSegments.put(currentSegment.start, currentSegment);
      }

      // Process jumpi branches.
      if (currentSegment.nextSegmentJump != CodeSegment.INVALID) {
        if (hasPathNotBeenWalked(currentSegment.nextSegmentJump, happyPathCodeSegments, sadPathCodeSegments)) {
          findPaths(currentSegment.nextSegmentJump, happyPathCodeSegments, sadPathCodeSegments);
        }
      }

      // Process the following code segment.
      if (currentSegment.nextSegmentNoJump == CodeSegment.INVALID) {
        foundEnd = true;
      }
      else {
        if (hasPathNotBeenWalked(currentSegment.nextSegmentNoJump, happyPathCodeSegments, sadPathCodeSegments)) {
          CodeSegment nextSegment = this.codeSegments.get(currentSegment.nextSegmentNoJump);
          if (nextSegment == null) {
            LOG.error("No code segment found for(2) {}", currentSegment.nextSegmentNoJump);
            throw new RuntimeException("No code segment found");
          }
          currentSegment = nextSegment;

        }
      }
    } while (!foundEnd);
  }

  private boolean hasPathNotBeenWalked(int start,
                               Map<Integer, CodeSegment> happyPathCodeSegments, Map<Integer, CodeSegment> sadPathCodeSegments) {
    return happyPathCodeSegments.get(start) == null && sadPathCodeSegments.get(start) == null;
  }


  /**
   * Check that all of code is accessed and that there are no duplicates
   *
   * @return
   */
  public boolean validateCodeSegments(int endOfCodeOffset) {
    LOG.info("Validating Code Segments");
    CodeSegment codeSegment = this.codeSegments.get(0);
    Set<Integer> futureCodeSegmentStart = new HashSet<>();
    while (true) {
      int nextSeg = codeSegment.start + codeSegment.length;
      LOG.info(" Start: {}, Next: {}", codeSegment.start, nextSeg);
      futureCodeSegmentStart.remove(nextSeg);

      if (codeSegment.nextSegmentJump > nextSeg) {
        futureCodeSegmentStart.add(codeSegment.nextSegmentJump);
      }
      else {
        if (codeSegment.nextSegmentJump != CodeSegment.INVALID && this.codeSegments.get(codeSegment.nextSegmentJump) == null) {
          LOG.error("Jump destination invalid for code segment: {}", codeSegment);
          throw new RuntimeException("Jump destination invalid");
        }
      }
      int lastOpCodeOffset = endOfCodeOffset-1;
      if (nextSeg == lastOpCodeOffset) {
        if (this.code.get(lastOpCodeOffset) == (byte)InvalidOperation.OPCODE) {
          if (futureCodeSegmentStart.isEmpty()) {
            return true;
          }
          LOG.info("Contained unused nextSegmentJumps: {}", futureCodeSegmentStart.size());
          return false;
        }
        LOG.info("Code correct length, but ends in opcode: {}", CodeVisitor.registry.get(this.code.get(lastOpCodeOffset), 0).getName());
        return false;
      }
      else {
        codeSegment = this.codeSegments.get(nextSeg);
        if (codeSegment == null) {
          int search = nextSeg + 1;
          while (codeSegment == null) {
            codeSegment = this.codeSegments.get(search++);
            if (search > this.codeSize) {
              LOG.error("Reached end of code at offset {}", this.codeSize);
              // TODO say the format is correct to differentiate from the errors above.
              return true;
            }
          }
          int nextSegStarts = search - 2;

          LOG.info(" *****Can't find segment for {}, length: {}. opCode({}) is {}, and opcode({}) is {}",
              nextSeg, nextSegStarts - nextSeg,
              nextSeg, getOpCodeString(nextSeg),
              nextSegStarts, getOpCodeString(nextSegStarts));
//          nextSeg = nextSegStarts;
        }
      }
    }
  }

  public boolean checkForUnusedCodeSegments() {
    Map<Integer, CodeSegment> unusedCodeSegs = new TreeMap<>();
    for (Integer startOfs: this.codeSegments.keySet()) {
      unusedCodeSegs.put(startOfs, this.codeSegments.get(startOfs));
    }

    for (Bytes funcId: this.allHappyPathCodeSegments.keySet()) {
      Map<Integer, CodeSegment> happyCodeSegs = this.allHappyPathCodeSegments.get(funcId);
      for (Integer startOfs: happyCodeSegs.keySet()) {
        unusedCodeSegs.remove(startOfs);
      }
    }
    for (Bytes funcId: this.allSadPathCodeSegments.keySet()) {
      Map<Integer, CodeSegment> sadPathCodeSegs = this.allSadPathCodeSegments.get(funcId);
      for (Integer startOfs: sadPathCodeSegs.keySet()) {
        unusedCodeSegs.remove(startOfs);
      }
    }

    for (Integer startOfs: unusedCodeSegs.keySet()) {
      LOG.info("Unused Code Segment: {}", unusedCodeSegs.get(startOfs));
    }
    return !unusedCodeSegs.isEmpty();
  }



  public void combineCodeSegments(int maxNumBytesBetweenCodeSegments) {
    for (Bytes functionId: this.allHappyPathCodeSegments.keySet()) {
//      LOG.info("Combining Code Segments for function: {}", functionId);

      Map<Integer, CodeSegment> happyPathCodeSegments = this.allHappyPathCodeSegments.get(functionId);
      Map<Integer, CodeSegment> sadPathCodeSegments = this.allSadPathCodeSegments.get(functionId);
      Map<Integer, CodeSegment> combinedCodeSegments = new TreeMap<>();

      TreeSet<Integer> happyPathSet = new TreeSet<>();
      happyPathSet.addAll(happyPathCodeSegments.keySet());
      Iterator<Integer> happyIter = happyPathSet.iterator();

      TreeSet<Integer> sadPathSet = new TreeSet<>();
      sadPathSet.addAll(sadPathCodeSegments.keySet());
      Iterator<Integer> sadIter = sadPathSet.iterator();


      int nextHappy = happyIter.next();
      if (nextHappy != 0) {
        throw new RuntimeException("Code didn't start at zero!");
      }
      CodeSegment newSegment = new CodeSegment(nextHappy, CodeSegment.INVALID);
      int startOfs = nextHappy;
      int len = happyPathCodeSegments.get(nextHappy).length;


      nextHappy = happyIter.next();
      int nextSad = sadIter.hasNext() ? sadIter.next() : CodeSegment.INVALID;

      do {
        if (nextHappy != CodeSegment.INVALID && nextSad != CodeSegment.INVALID) {
          if (nextHappy < nextSad) {
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
          } else {
            if (nextSad - maxNumBytesBetweenCodeSegments <= startOfs + len) {
              // Combine segments
              len = nextSad - startOfs + sadPathCodeSegments.get(nextSad).length;
            } else {
              newSegment.setValuesLengthOnly(len);
              combinedCodeSegments.put(startOfs, newSegment);
              startOfs = nextSad;
              len = sadPathCodeSegments.get(nextHappy).length;
              newSegment = new CodeSegment(startOfs, CodeSegment.INVALID);
            }
            nextSad = sadIter.hasNext() ? sadIter.next() : CodeSegment.INVALID;
          }
        }
        else if (nextHappy != CodeSegment.INVALID) {
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
        }
        else {
          if (nextSad - maxNumBytesBetweenCodeSegments <= startOfs + len) {
            // Combine segments
            len = nextSad - startOfs + sadPathCodeSegments.get(nextSad).length;
          } else {
            newSegment.setValuesLengthOnly(len);
            combinedCodeSegments.put(startOfs, newSegment);
            startOfs = nextSad;
            len = sadPathCodeSegments.get(nextHappy).length;
            newSegment = new CodeSegment(startOfs, CodeSegment.INVALID);
          }
          nextSad = sadIter.hasNext() ? sadIter.next() : CodeSegment.INVALID;
        }
      } while (nextHappy != CodeSegment.INVALID || nextSad != CodeSegment.INVALID);
      // Don't forget to do the final segment!
      newSegment.setValuesLengthOnly(len);
      combinedCodeSegments.put(startOfs, newSegment);

      this.allCombinedCodeSegments.put(functionId, combinedCodeSegments);

      LOG.info("Combined CodeSegments: Function: {}, Prior to Combination(Happy: {}, Sad: {}), Combined: {}",
          functionId, happyPathCodeSegments.size(), sadPathCodeSegments.size(), combinedCodeSegments.size());
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
    Operation opCode = CodeVisitor.registry.get(opCodeValue, 0);
    if (opCode != null) {
      return opCode.getName();
    }
    else {
      return Integer.toHexString(opCodeValue);
    }
  }
}
