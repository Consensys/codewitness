package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.tuweni.bytes.Bytes;

public class BasicBlockWithCode {
  private int start;
  private int length;
  private Bytes codeFragment;

  public BasicBlockWithCode(int start, int length, Bytes codeFragment) {
    this.start = start;
    this.length = length;
    this.codeFragment = codeFragment;
  }

  public int getStart() {
    return start;
  }

  public int getLength() {
    return length;
  }

  public Bytes getCodeFragment() {
    return codeFragment;
  }
}
