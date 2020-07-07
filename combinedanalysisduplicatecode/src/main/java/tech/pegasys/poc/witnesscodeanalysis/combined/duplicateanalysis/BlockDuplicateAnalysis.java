package tech.pegasys.poc.witnesscodeanalysis.combined.duplicateanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.functionid.CodeVisitor;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdMerklePatriciaTrieLeafData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.logging.log4j.LogManager.getLogger;

public class BlockDuplicateAnalysis {
  private static final Logger LOG = getLogger();

  // Contract id, Start, Length
  private Map<Integer, Map<Integer, Integer>> contractCodeExecutedUniqueCode = new HashMap<>();
  // Contract address, Start, Length
  private Map<String, Map<Integer, Integer>> contractCodeExecutedPerAddress = new HashMap<>();

  private int functionIdRawLeafSizeWithoutCode = 0;
  private int functionIdNumberOfLeaves = 0;
  private int functionIdWitnessSizeUniqueCode = 0;
  private int functionIdWitnessSizePerAddress = 0;


  private int failContractIdNotFoundInFunctionIdDataSet = 0;
  private int failContractNotProcessedCorrectlyByFunctionIdAnalysis = 0;
  private int failAllCodeLeafNotFound = 0;

  private int failContractIdNotFoundInJumpDestDataSet = 0;
  private int jumpDestWitnessSize = 0;

  private int failContractIdNotFoundInFixedDataSet = 0;
  private int fixedWitnessSize = 0;

  private int failContractIdNotFoundInStrictDataSet = 0;
  private int strictWitnessSize = 0;


  public void processTransactionCall(int id, String toAddress, Bytes functionSelector) throws IOException {
    FunctionIdAllResult result = functionIdResult(id);
    if (result == null) {
      LOG.error("Contract id {} not found in FunctionId dataset!", id);
      this.failContractIdNotFoundInFunctionIdDataSet++;
      return;
    }
    UnableToProcessReason processingResult = result.getResult();
    if (processingResult != UnableToProcessReason.SUCCESS) {
      LOG.trace(" Contract {} was not processed correctly by function id analysis: {}", id, processingResult);
      this.failContractNotProcessedCorrectlyByFunctionIdAnalysis++;
      // Skip processing
      return;
    }

    if (functionSelector.isEmpty()) {
      functionSelector = CodeVisitor.FALLBACK_FUNCTION_FUNCTIONID;
    }

    FunctionIdMerklePatriciaTrieLeafData leaf = result.getLeaf(functionSelector);
    if (leaf == null) {
      leaf = result.getLeaf(CodeVisitor.ALL_CODE_FUNCTIONID);
    }
    if (leaf == null) {
      LOG.error(" Can't find all code leaf for contract {}", id);
      this.failAllCodeLeafNotFound++;
      return;
    }
    BasicBlockWithCode[] codeBlocks = leaf.getBasicBlocksWithCode();

    Map<Integer, Integer> contractCodeBlocks = this.contractCodeExecutedUniqueCode.get(id);
    if (contractCodeBlocks == null) {
      contractCodeBlocks = new HashMap<>();
      this.contractCodeExecutedUniqueCode.put(id, contractCodeBlocks);
    }
    for (BasicBlockWithCode codeBlock: codeBlocks) {
      contractCodeBlocks.put(codeBlock.getStart(), codeBlock.getLength());
    }

    Map<Integer, Integer> contractCodeBlocks1 = this.contractCodeExecutedPerAddress.get(toAddress);
    if (contractCodeBlocks1 == null) {
      contractCodeBlocks1 = new HashMap<>();
      this.contractCodeExecutedPerAddress.put(toAddress, contractCodeBlocks1);
    }
    for (BasicBlockWithCode codeBlock: codeBlocks) {
      contractCodeBlocks1.put(codeBlock.getStart(), codeBlock.getLength());
    }
  }

  public void calculateLeafPlusCodeSizes() throws IOException {
    // Find the amount of code sent.
    int functionIdCodeUsed = 0;
    for (Map<Integer, Integer> codeBlocks : this.contractCodeExecutedUniqueCode.values()) {
      for (Integer lengths : codeBlocks.values()) {
        functionIdCodeUsed += lengths;
      }
    }
    // TODO need to calculate function id leaf size properly
    // TODO ignore leaf size that isn't code for the moment.

    // TODO need to combine in the proof piece of this.
    this.functionIdWitnessSizeUniqueCode = functionIdCodeUsed + this.functionIdRawLeafSizeWithoutCode;

    functionIdCodeUsed = 0;
    for (Map<Integer, Integer> codeBlocks : this.contractCodeExecutedPerAddress.values()) {
      for (Integer lengths : codeBlocks.values()) {
        functionIdCodeUsed += lengths;
      }
    }
    // TODO need to calculate function id leaf size properly
    // TODO ignore leaf size that isn't code for the moment.

    // TODO need to combine in the proof piece of this.
    this.functionIdWitnessSizePerAddress = functionIdCodeUsed + this.functionIdRawLeafSizeWithoutCode;


  }


  private static Map<Integer, FunctionIdAllResult> functionIdEverything = new HashMap<>();
  public FunctionIdAllResult functionIdResult(int id) throws IOException {
    if (functionIdEverything.isEmpty()) {
      LOG.info("Loading analysis_functionid.json");
      FunctionIdDataSetReader functionIdDataSetReader = new FunctionIdDataSetReader();
      FunctionIdAllResult result;
      while ((result = functionIdDataSetReader.next()) != null) {
        functionIdEverything.put(result.getId(), result);
      }
    }

    return functionIdEverything.get(id);
  }

  public void showStats() {
    LOG.info("  functionIdWitnessSize Unique Code: {} ", this.functionIdWitnessSizeUniqueCode);
    LOG.info("  functionIdWitnessSize Per Address: {}", this.functionIdWitnessSizePerAddress);

    LOG.info("  failContractIdNotFoundInFunctionIdDataSet: {} ", this.failContractIdNotFoundInFunctionIdDataSet);
    LOG.info("  failContractNotProcessedCorrectlyByFunctionIdAnalysis: {} ", this.failContractNotProcessedCorrectlyByFunctionIdAnalysis);
    LOG.info("  failAllCodeLeafNotFound: {} ", this.failAllCodeLeafNotFound);
  }

  public void setResultInformation(WitnessResultDuplicate result) {
    result.unique = this.functionIdWitnessSizeUniqueCode;
    result.peraddr = this.functionIdWitnessSizePerAddress;
    result.fail = this.failContractNotProcessedCorrectlyByFunctionIdAnalysis;
  }

}
