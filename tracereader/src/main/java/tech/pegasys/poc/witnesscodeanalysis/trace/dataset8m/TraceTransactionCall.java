package tech.pegasys.poc.witnesscodeanalysis.trace.dataset8m;

import org.apache.tuweni.bytes.Bytes;

public class TraceTransactionCall {
  //       {"callType":"call","from":"0xc6a8e24a8e30fd4bafc61f4a98238cedddf99a1f","gas":"0x9858","input":"0x","to":"0x3b921f0f543cfdb3fe9797911267e14382d8db7b","value":"0x1f0ef9247f530000"},

  private String callType;
  private String from;
  private String gas;
  private String input;
  private String to;
  private String value;

  public String getCallType() {
    return callType;
  }

  public String getFrom() {
    return from;
  }

  public String getGas() {
    return gas;
  }

  public String getInput() {
    return input;
  }

  public Bytes getFunctionSelector() {
    if (this.input == null || this.input.length() <= 2) {
      return Bytes.EMPTY;
    }
    else {
      int len = this.input.length();
      if (len < 10) {
        return Bytes.fromHexString(this.input.substring(2, len));
      }
      return Bytes.fromHexString(this.input.substring(2, 10));
    }
  }

  public String getTo() {
    return to;
  }

  public String getValue() {
    return value;
  }


}
