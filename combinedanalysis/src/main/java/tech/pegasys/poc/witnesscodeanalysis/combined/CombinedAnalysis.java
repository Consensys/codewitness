package tech.pegasys.poc.witnesscodeanalysis.combined;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.DeployDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.TraceBlockData;
import tech.pegasys.poc.witnesscodeanalysis.trace.TraceDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.TraceTransactionCall;
import tech.pegasys.poc.witnesscodeanalysis.trace.TraceTransactionData;
import tech.pegasys.poc.witnesscodeanalysis.trace.TraceTransactionInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CombinedAnalysis {
  private static final Logger LOG = getLogger();
  private static Map<String, Integer> contractsAddresToId;

  private Set<String> unknownContracts = new TreeSet<>();

  private WitnessResultWriter writer;


  public CombinedAnalysis() throws IOException {
    this.writer = new WitnessResultWriter();
  }

  public void go() throws IOException {
    LOG.info("Loading contract to id mappings");
    contractsAddresToId = DeployDataSetReader.getContractsToId();
    LOG.info("Loaded {} contract to id mappings", contractsAddresToId.size());

    //processBlock(8200000); //, 8203459
//    processBlocks(8200000, 8200002);
 processBlocks(8200000, 8203459);

    LOG.info("Unknwon Contracts");
    for (String unkownContract: this.unknownContracts) {
      LOG.info(" {}", unkownContract);
    }

    this.writer.close();
  }

  public void processBlocks(int from, int to) throws IOException {
    for (int i = from; i <= to; i++) {
      processBlock(i);
    }
  }


  public void processBlock(int blockNumber) throws IOException {
    LOG.info("Processing block number: {}", blockNumber);
    BlockAnalysis blockAnalysis = new BlockAnalysis();

    TraceDataSetReader dataSet = new TraceDataSetReader(blockNumber);
    // There is exactly one block per data set.
    TraceBlockData blockData = dataSet.next();


    TraceTransactionData[] transactionsData = blockData.getBlock();
    LOG.info(" Block contains: {} transactions", transactionsData.length);

    int zz = 0;
    for (TraceTransactionData transactionData: transactionsData) {
      LOG.trace(" Processing transaction");
      TraceTransactionInfo[] infos = transactionData.getTrace();
      LOG.trace("  Transaction contains: {} calls", infos.length);

      boolean stop = false;
      for (TraceTransactionInfo info: infos) {
          TraceTransactionCall call = info.getAction();
          String toAddress = call.getTo();
          Bytes functionSelector = call.getFunctionSelector();

          Integer id = contractsAddresToId.get(toAddress);
          if (id == null) {
            if (functionSelector.isEmpty()) {
              LOG.trace("   Value Transfer transaction");
            }
            else {
              LOG.error("   Unknown contract {}. Function call: {}", toAddress, functionSelector);
              this.unknownContracts.add(toAddress);
            }
          }
          else {
            LOG.info("   Call to contract({}): {}, function {}", id, toAddress, functionSelector);
            blockAnalysis.processTransactionCall(id, functionSelector);
            // TODO
            zz++;
            if (zz > 1) {
              stop = true;
              break;
            }
          }
      }
      if (stop)
      {
        break;
      }
    }
    dataSet.close();

    blockAnalysis.calculateLeafPlusCodeSizes();
    blockAnalysis.showStats();
    WitnessResult result = new WitnessResult(blockNumber);
    blockAnalysis.setResultInformation(result);
    this.writer.writeResult(result);
  }

  public static void main(String[] args) throws Exception {
    (new CombinedAnalysis()).go();
  }

}
