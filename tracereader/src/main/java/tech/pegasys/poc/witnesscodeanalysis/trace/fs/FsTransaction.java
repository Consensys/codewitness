package tech.pegasys.poc.witnesscodeanalysis.trace.fs;

import org.apache.tuweni.bytes.Bytes;

public class FsTransaction {
  public String txHash;
  public String to;
  public String functionSelector;

  public FsTransaction(String txHash, String to, String functionSelector) {
    this.txHash = txHash;
    this.to = to;
    this.functionSelector = functionSelector;
  }

  public Bytes getFunctionSelector() {
    if (this.functionSelector == null || this.functionSelector.length() <= 2) {
      return Bytes.EMPTY;
    }
    else {
      return Bytes.fromHexString(this.functionSelector.substring(2));
    }
  }

  public String getTo() {
    return to;
  }

}
