package tech.pegasys.poc.witnesscodeanalysis.trace.dataset8m;

public class TraceTransactionInfo {
  //    {"action":
  //       {"callType":"call","from":"0xc6a8e24a8e30fd4bafc61f4a98238cedddf99a1f","gas":"0x9858","input":"0x","to":"0x3b921f0f543cfdb3fe9797911267e14382d8db7b","value":"0x1f0ef9247f530000"},
  //    "result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}

  private TraceTransactionCall action;
  private TraceTransactionResult result;
  private int subtraces;
  private String[] traceAddress;
  private String type;

  public TraceTransactionCall getAction() {
    return action;
  }
}
