package tech.pegasys.poc.witnesscodeanalysis.combined.duplicateanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.DeployDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FsBlock;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FsTransaction;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FunctionSelectorDataSetReader;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;


// Check to see if the same code, via different deployed contracts, is often accessed in the same block
public class DuplicateAnalysis {
  private static final Logger LOG = getLogger();
  private static Map<String, Integer> contractsAddresToId;

  private Set<String> unknownContracts = new TreeSet<>();

  private WitnessResultDuplicateWriter writer;


  public DuplicateAnalysis() throws IOException {
    this.writer = new WitnessResultDuplicateWriter();
  }

  public void go() throws IOException {
    LOG.info("Loading contract to id mappings");
    contractsAddresToId = DeployDataSetReader.getContractsToId();
    LOG.info("Loaded {} contract to id mappings", contractsAddresToId.size());

// processBlocks(8200000, 8203459);
    processBlocks(9000000, 9000100);

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
    BlockDuplicateAnalysis blockAnalysis = new BlockDuplicateAnalysis();

    try {
      FunctionSelectorDataSetReader dataSet = new FunctionSelectorDataSetReader(blockNumber);
      // There is exactly one block per data set.
      FsBlock blockData = dataSet.read();


      FsTransaction[] transactionsData = blockData.getTransactions();
      LOG.info(" Block {} contains: {} transactions", blockData.getBlockNumber(), transactionsData.length);

      //    int zz = 0;
      for (FsTransaction transactionData : transactionsData) {
        String toAddress = transactionData.getTo();
        Bytes functionSelector = transactionData.getFunctionSelector();

        Integer id = contractsAddresToId.get(toAddress);
        if (id == null) {
          if (functionSelector.isEmpty()) {
            LOG.trace("   Value Transfer transaction");
          } else {
            LOG.error("   Unknown contract {}. Function call: {}", toAddress, functionSelector);
            this.unknownContracts.add(toAddress);
          }
        } else {
          LOG.info("   Call to contract({}): {}, function {}", id, toAddress, functionSelector);
          blockAnalysis.processTransactionCall(id, toAddress, functionSelector);
          //            // TODO
          //            zz++;
          //            if (zz > 1) {
          //              stop = true;
          //              break;
          //            }
        }
        //      if (stop)
        //      {
        //        break;
        //      }
      }
      dataSet.close();

      blockAnalysis.calculateLeafPlusCodeSizes();
      blockAnalysis.showStats();
      WitnessResultDuplicate result = new WitnessResultDuplicate(blockNumber);
      blockAnalysis.setResultInformation(result);
      this.writer.writeResult(result);
      this.writer.flush();
    } catch (Exception ex) {
      LOG.error("  Exception while processing block {}: {}", blockNumber, ex.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    (new DuplicateAnalysis()).go();
  }

}
