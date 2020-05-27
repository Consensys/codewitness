package tech.pegasys.poc.witnesscodeanalysis.functionid;

import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;

import java.math.BigInteger;

public class CodeSegment {
  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public static final int INVALID = -1;
  public int start;
  public int length = INVALID;
  public boolean endsProgram = false;
  public boolean happyPathEnding = false;
  public int previousSegment;
  public int nextSegmentNoJump = INVALID;
  public int nextSegmentJump = INVALID;
  private int lastOpCode = INVALID;

  public CodeSegment(int start, int callingSegmentPc) {
    this.start = start;
    this.previousSegment = callingSegmentPc;
  }

  // Jump or fall through, does not end.
  public void setValuesJumpi(int len, int nextSegmentJump, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.nextSegmentJump = nextSegmentJump;
    this.nextSegmentNoJump = this.start + len;
    this.lastOpCode = lastOpCode;
  }

  // Fall through only, does not end.
  public void setValuesJumpDest(int len, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.nextSegmentNoJump = this.start + len;
    this.lastOpCode = lastOpCode;
  }

  // Jump only, does not end.
  public void setValuesJump(int len, int nextSegmentJump, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.nextSegmentJump = nextSegmentJump;
    this.lastOpCode = lastOpCode;
  }

  // Return, does not end.
  public void setValuesReturnSub(int len, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.lastOpCode = lastOpCode;
  }

  public void setValuesHappyEnding(int len, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.lastOpCode = lastOpCode;
    this.endsProgram = true;
    this.happyPathEnding = true;
  }

  public void setValuesSadEnding(int len, int lastOpCode) {
    checkNotSet();
    this.length = len;
    this.lastOpCode = lastOpCode;
    this.endsProgram = true;
  }

  public void setValuesLengthOnly(int len) {
    checkNotSet();
    this.length = len;
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
    buf.append(this.previousSegment);
    buf.append(", start: ");
    buf.append(this.start);
    buf.append(", length: ");
    buf.append(this.length);
    buf.append(", next: ");
    buf.append(this.nextSegmentNoJump);
    buf.append(", jumpdest: ");
    buf.append(this.nextSegmentJump);
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
