package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
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

import static org.apache.logging.log4j.LogManager.getLogger;


public class CodeVisitor {
  private static final Logger LOG = getLogger();
  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  Bytes code;
  Map<Integer, CodeSegment> codeSegments;
  Map<Bytes, Integer> foundFunctions;

  public CodeVisitor(Bytes code, Map<Integer, CodeSegment> codeSegments, Map<Bytes, Integer> foundFunctions) {
    this.code = code;
    this.codeSegments = codeSegments;
    this.foundFunctions = foundFunctions;
  }

  public void visit(MessageFrame frame, int callingSegmentPc) {
    int pc = frame.getPC();
    int startingPc = pc;
    addCodeSegment(startingPc, callingSegmentPc);
    CodeSegment codeSegment = this.codeSegments.get(startingPc);
    boolean done = false;

    boolean foundPush4OpCode = false;
    boolean foundEqOpCode = false;
    boolean foundPushDestAddress = false;
    Bytes functionId = null;

    while (!done) {
      final Operation curOp = registry.get(code.get(pc), 0);
      int opCode = curOp.getOpcode();
      frame.setCurrentOperation(curOp);
      curOp.execute(frame);

      // Find function entry points. Look for code matching the pattern shown below:
      // PUSH4 0x95CACBE0
      // EQ
      // PUSH1 0x41
      // JUMPI
      if (foundPushDestAddress) {
        foundPushDestAddress = false;
        if (opCode == JumpiOperation.OPCODE) {
          LOG.info("****Found function {} in code segment {}", functionId, startingPc);
          foundFunctions.put(functionId, startingPc);
        }
      }
      if (foundEqOpCode) {
        foundEqOpCode = false;
        if (opCode == PushOperation.PUSH1_OPCODE || opCode == PushOperation.PUSH2_OPCODE) {
          foundPushDestAddress = true;
        }
      }
      if (foundPush4OpCode) {
        foundPush4OpCode = false;
        if (opCode == EqOperation.OPCODE) {
          foundEqOpCode = true;
        }
      }
      if (opCode == PushOperation.PUSH4_OPCODE) {
        functionId = frame.getStackItem(0).slice(28, 4);
        foundPush4OpCode = true;
      }


      // Process jumps.
      int jumpDest = curOp.jumpDest().intValue();
      if (opCode == JumpiOperation.OPCODE || opCode == JumpOperation.OPCODE) {
        LOG.info("PC1: {}, Operation {}, Jump Destination: {}", pc, curOp.getName(), jumpDest);
        if (this.codeSegments.get(jumpDest) == null) {
          // Not visited yet.
          MessageFrame newMessageFrame = (MessageFrame) frame.clone();
          newMessageFrame.setPC(jumpDest);
          visit(newMessageFrame, startingPc);
        }
        else {
          LOG.info("**I have been to {} before!", jumpDest);
        }
      }
      else {
        LOG.info("PC1: {}, Operation {}", pc, curOp.getName());
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
          if (this.codeSegments.get(startingPc) != null) {
            LOG.info("**Falling through to existing segment: {}", pc);
            return;
          }
          addCodeSegment(startingPc, callingSegmentPc);
          codeSegment = this.codeSegments.get(startingPc);
          break;
        case JumpDestOperation.OPCODE:
          // End a code segments immediately before JUMPDEST operations, if
          // the code has fallen through rather than jumped here.
          if (pc - opSize != startingPc) {
            codeSegment.setValuesJumpDest(pc - startingPc - opSize, opCode);
            callingSegmentPc = startingPc;
            startingPc = pc - opSize;
            if (this.codeSegments.get(startingPc) != null) {
              LOG.info("**Falling through to existing segment: {}", pc);
              return;
            }
            addCodeSegment(startingPc, callingSegmentPc);
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

  private void addCodeSegment(int startingPc, int callingSegmentPc) {
    CodeSegment existingCodeSegent = this.codeSegments.get(startingPc);
    if (existingCodeSegent != null) {
      LOG.error("Code Segment already exists for start PC {}: {}", startingPc, existingCodeSegent);
      throw new RuntimeException("Code segment already exists");
    }
    CodeSegment codeSegment = new CodeSegment(startingPc, callingSegmentPc);
    this.codeSegments.put(startingPc, codeSegment);
  }

}
