package tech.pegasys.poc.witnesscodeanalysis.trace.dataset8m;

public class TraceTransactionData {
  // {"output":"0x","stateDiff":null,"trace":[
  //    {"action":
  //       {"callType":"call","from":"0xc6a8e24a8e30fd4bafc61f4a98238cedddf99a1f","gas":"0x9858","input":"0x","to":"0x3b921f0f543cfdb3fe9797911267e14382d8db7b","value":"0x1f0ef9247f530000"},
  //    "result":{"gasUsed":"0x0","output":"0x"},"subtraces":0,"traceAddress":[],"type":"call"}
  //   ],
  //  "transactionHash":"0x0cd608fee14619f52b6f27e7b89034b94d0449dd303bfb82584098f8cd2c4840","vmTrace":null
  // }

  private String output;
  private String stateDiff;
  private TraceTransactionInfo[] trace;
  private String transactionHash;
  private String vmTrace;

  public TraceTransactionData(final String output, final String stateDiff, final TraceTransactionInfo[] trace, final String transactionHash, String vmTrace) {
    this.output = output;
    this.stateDiff = stateDiff;
    this.trace = trace;
    this.transactionHash = transactionHash;
    this.vmTrace = vmTrace;
  }

  public TraceTransactionInfo[] getTrace() {
    return trace;
  }
}
