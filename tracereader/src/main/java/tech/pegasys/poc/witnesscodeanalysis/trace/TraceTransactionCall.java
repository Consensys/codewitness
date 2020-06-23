package tech.pegasys.poc.witnesscodeanalysis.trace;

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

  public String getTo() {
    return to;
  }

  public String getValue() {
    return value;
  }


}
