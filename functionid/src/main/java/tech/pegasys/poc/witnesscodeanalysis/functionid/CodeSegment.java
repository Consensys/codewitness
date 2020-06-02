package tech.pegasys.poc.witnesscodeanalysis.functionid;

import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperandStack;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CodeSegment {
  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public static final int INVALID = -1;
  public int start;
  public int length = INVALID;
  public boolean endsProgram = false;
  public boolean happyPathEnding = false;
  public ArrayList<Integer> previousSegments = new ArrayList<>();
  public ArrayList<OperandStack> previousSegmentStacks = new ArrayList<>();
  public int nextSegmentNoJump = INVALID;
  public Set<Integer> nextSegmentJumps = new HashSet<>();
  private int lastOpCode = INVALID;

  public CodeSegment(int start) {
    this.start = start;
  }


  public CodeSegment(int start, int callingSegmentPc, OperandStack callingSegmentStack) {
    this.start = start;
    this.previousSegments.add(callingSegmentPc);
    this.previousSegmentStacks.add(callingSegmentStack);
  }

  public void addNewPrevious(int callingSegmentPc, OperandStack callingSegmentStack) {
    this.previousSegments.add(callingSegmentPc);
    this.previousSegmentStacks.add(callingSegmentStack);
  }

  // Jump or fall through, does not end.
  public void setValuesJumpi(int len, int nextSegmentJump, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    addJumpIfNotSetAlready(nextSegmentJump);
    setNoJump();
  }

  // Fall through only, does not end.
  public void setValuesJumpDest(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    setNoJump();
  }

  // Jump only, does not end.
  public void setValuesJump(int len, int nextSegmentJump, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    addJumpIfNotSetAlready(nextSegmentJump);
  }

  // Return, does not end.
  public void setValuesReturnSub(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
  }

  public void setValuesHappyEnding(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    this.endsProgram = true;
    this.happyPathEnding = true;
  }

  public void setValuesSadEnding(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    this.endsProgram = true;
  }

  public void setValuesLengthOnly(int len) {
    checkNotSet();
    this.length = len;
  }

  private void setLenLastOpCode(int len, int lastOpCode) {
    if (this.length != INVALID) {
      if (this.length != len && this.lastOpCode != lastOpCode) {
        throw new Error("Setting ReturnSub a second time with inconsistent values");
      }
      return;
    }
    this.length = len;
    this.lastOpCode = lastOpCode;
  }

  private void addJumpIfNotSetAlready(int nextSegmentJump) {
    this.nextSegmentJumps.add(nextSegmentJump);
  }

  private void setNoJump() {
    // If the start and the length are known, then the next segment no jump will always be the same.
    this.nextSegmentNoJump = this.start + this.length;
  }


  private void checkNotSet() {
    if (this.length != INVALID) {
      throw new RuntimeException("Code segment data already set. Start offset: " + this.start);
    }
  }


  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Code Segment: previous: ");
    if (this.previousSegments.isEmpty()) {
      buf.append("none, ");
    }
    else {
      for (int previousSegment: this.previousSegments) {
        buf.append(previousSegment);
        buf.append(", ");
      }
    }
    buf.append("start: ");
    buf.append(this.start);
    buf.append(", length: ");
    buf.append(this.length);
    buf.append(", next: ");
    buf.append(this.nextSegmentNoJump);
    buf.append(", jumpdest: ");
    if (this.nextSegmentJumps.isEmpty()) {
      buf.append("none, ");
    } else {
      for (int nextSegmentJump: this.nextSegmentJumps) {
        buf.append(nextSegmentJump);
        buf.append(", ");
      }
    }
    buf.append(", End of program: ");
    buf.append(this.endsProgram);
    if (this.endsProgram) {
      buf.append(", Happy Path Ending: ");
      buf.append(this.happyPathEnding);
    }
    if (this.lastOpCode != INVALID) {
      buf.append(", Last OpCode: ");
      buf.append(registry.get(this.lastOpCode, 0).getName());
    }
    return buf.toString();
  }
}
