package tech.pegasys.poc.witnesscodeanalysis.trace;

import org.apache.logging.log4j.Logger;


import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Assumes trace files are in ./traces directory.
 */
public class ShowTraceData {
  private static final Logger LOG = getLogger();

  public void showRange(int begin, int end) throws Exception {
    for (int i=begin; i < end; i++) {
      show(i);
    }
  }

  public void show(int blockNumber) throws Exception {
    LOG.info("Show block number: {}", blockNumber);

    TraceDataSetReader dataSet = new TraceDataSetReader(blockNumber);

    TraceBlockData blockData;
    while ((blockData = dataSet.next()) != null) {
      TraceTransactionData[] transactionsData = blockData.getBlock();
      LOG.info(" Block contains: {} transactions", transactionsData.length);

      for (TraceTransactionData transactionData: transactionsData) {
        TraceTransactionInfo[] infos = transactionData.getTrace();
        LOG.info(" Transaction contains: {} infos", infos.length);
        for (TraceTransactionInfo info: infos) {
          TraceTransactionCall call = info.getAction();
          LOG.info("Call type: {}" ,call.getCallType());
          LOG.info("Call From: {}", call.getFrom());
          LOG.info("Call To: {}", call.getTo());
          LOG.info("Call Data: {}", call.getInput());
          LOG.info("Call Gas: {}", call.getGas());
          LOG.info("Call value: {}", call.getValue());
        }

      }
    }
    dataSet.close();
  }





  public static void main(String[] args) throws Exception {
    ShowTraceData trace = new ShowTraceData();

    trace.showRange(8200000, 8203459);
  }


}
