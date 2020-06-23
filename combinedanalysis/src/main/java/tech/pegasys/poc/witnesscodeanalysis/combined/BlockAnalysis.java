package tech.pegasys.poc.witnesscodeanalysis.combined;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkData;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkDataReader;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.functionid.CodeVisitor;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdMerklePatriciaTrieLeafData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;

public class BlockAnalysis {
  private static final Logger LOG = getLogger();

  // Contract id, Start, Length
  private Map<Integer, Map<Integer, Integer>> contractCodeExecuted = new HashMap<>();

  private int functionIdRawLeafSizeWithoutCode = 0;
  private int functionIdNumberOfLeaves = 0;
  private int functionIdWitnessSize = 0;


  private int failContractIdNotFoundInFunctionIdDataSet = 0;
  private int failContractNotProcessedCorrectlyByFunctionIdAnalysis = 0;
  private int failAllCodeLeafNotFound = 0;

  private int failContractIdNotFoundInJumpDestDataSet = 0;
  private int jumpDestWitnessSize = 0;

  private int failContractIdNotFoundInFixedDataSet = 0;
  private int fixedWitnessSize = 0;

  private int failContractIdNotFoundInStrictDataSet = 0;
  private int strictWitnessSize = 0;

  public void processTransactionCall(int id, Bytes functionSelector) throws IOException {
    FunctionIdDataSetReader functionIdDataSetReader = new FunctionIdDataSetReader();

    FunctionIdAllResult result;
    boolean found = false;
    while ((result = functionIdDataSetReader.next()) != null) {
      if (result.getId() == id) {
        found = true;
        break;
      }
    }
    if (!found) {
      LOG.error("Contract id {} not found in FunctionId dataset!", id);
      this.failContractIdNotFoundInFunctionIdDataSet++;
      return;
    }
    UnableToProcessReason processingResult = result.getResult();
    if (processingResult != UnableToProcessReason.SUCCESS) {
      LOG.error(" Contract {} was not processed correctly by function id analysis: {}", id, processingResult);
      this.failContractNotProcessedCorrectlyByFunctionIdAnalysis++;
      functionSelector = CodeVisitor.ALL_CODE_FUNCTIONID;
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

    Map<Integer, Integer> contractCodeBlocks = this.contractCodeExecuted.get(id);
    if (contractCodeBlocks == null) {
      contractCodeBlocks = new HashMap<>();
      this.contractCodeExecuted.put(id, contractCodeBlocks);
    }
    for (BasicBlockWithCode codeBlock: codeBlocks) {
      contractCodeBlocks.put(codeBlock.getStart(), codeBlock.getLength());
    }

    // Do part of the function id analysis here.
    this.functionIdRawLeafSizeWithoutCode += codeBlocks.length * 4 + 4;
    this.functionIdNumberOfLeaves++;
  }

  public void calculateLeafPlusCodeSizes() throws IOException {

    // Find the amount of code sent.
    int functionIdCodeUsed = 0;
    for (Map<Integer, Integer> codeBlocks : this.contractCodeExecuted.values()) {
      for (Integer lengths : codeBlocks.values()) {
        functionIdCodeUsed += lengths;
      }
    }
    // TODO need to combine in the proof piece of this.
    this.functionIdWitnessSize = functionIdCodeUsed + this.functionIdRawLeafSizeWithoutCode;

    int jumpDestDataUsed = 0;
    for (Integer id : this.contractCodeExecuted.keySet()) {
      Map<Integer, Integer> codeBlocks = this.contractCodeExecuted.get(id);
      ChunkData chunkData = getJumpDestLeaves(id);
      Map<Integer, Integer> startOffsetsAndLengths = chunkData.getChunks();

      Set<Integer> chunksUsed = new TreeSet<>();
      for (Integer start: codeBlocks.keySet()) {
        int length = codeBlocks.get(start);
        // Determine the chunks needed for this code block.
        int lastChunkStart = 0;
        int chunkStart = 0;
        Iterator<Integer> chunkStartIter = startOffsetsAndLengths.keySet().iterator();
        while (chunkStartIter.hasNext()) {
          lastChunkStart = chunkStart;
          chunkStart = chunkStartIter.next();
          if (chunkStart > start) {
            chunksUsed.add(lastChunkStart);
          }
          while (chunkStart < start+length) {
            lastChunkStart = chunkStart;
            chunkStart = chunkStartIter.next();
            chunksUsed.add(chunkStart);
          }
        }
      }

      for (Integer startUsed: chunksUsed) {
        jumpDestDataUsed += startOffsetsAndLengths.get(startUsed);
      }
    }
    this.jumpDestWitnessSize = jumpDestDataUsed;




    int fixedDataUsed = 0;
    for (Integer id : this.contractCodeExecuted.keySet()) {
      Map<Integer, Integer> codeBlocks = this.contractCodeExecuted.get(id);
      ChunkData chunkData = getFixedLeaves(id);
      Map<Integer, Integer> startOffsetsAndLengths = chunkData.getChunks();

      Set<Integer> chunksUsed = new TreeSet<>();
      for (Integer start: codeBlocks.keySet()) {
        int length = codeBlocks.get(start);
        // Determine the chunks needed for this code block.
        int lastChunkStart = 0;
        int chunkStart = 0;
        Iterator<Integer> chunkStartIter = startOffsetsAndLengths.keySet().iterator();
        while (chunkStartIter.hasNext()) {
          lastChunkStart = chunkStart;
          chunkStart = chunkStartIter.next();
          if (chunkStart > start) {
            chunksUsed.add(lastChunkStart);
          }
          while (chunkStart < start+length) {
            lastChunkStart = chunkStart;
            chunkStart = chunkStartIter.next();
            chunksUsed.add(chunkStart);
          }
        }
      }

      for (Integer startUsed: chunksUsed) {
        fixedDataUsed += startOffsetsAndLengths.get(startUsed);
      }
    }
    this.fixedWitnessSize = fixedDataUsed;

    int strictDataUsed = 0;
    for (Integer id : this.contractCodeExecuted.keySet()) {
      Map<Integer, Integer> codeBlocks = this.contractCodeExecuted.get(id);
      ChunkData chunkData = getStrictLeaves(id);
      Map<Integer, Integer> startOffsetsAndLengths = chunkData.getChunks();

      Set<Integer> chunksUsed = new TreeSet<>();
      for (Integer start: codeBlocks.keySet()) {
        int length = codeBlocks.get(start);
        // Determine the chunks needed for this code block.
        int lastChunkStart = 0;
        int chunkStart = 0;
        Iterator<Integer> chunkStartIter = startOffsetsAndLengths.keySet().iterator();
        while (chunkStartIter.hasNext()) {
          lastChunkStart = chunkStart;
          chunkStart = chunkStartIter.next();
          if (chunkStart > start) {
            chunksUsed.add(lastChunkStart);
          }
          while (chunkStart < start+length) {
            lastChunkStart = chunkStart;
            chunkStart = chunkStartIter.next();
            chunksUsed.add(chunkStart);
          }
        }
      }

      for (Integer startUsed: chunksUsed) {
        strictDataUsed += startOffsetsAndLengths.get(startUsed);
      }
    }
    this.strictWitnessSize = strictDataUsed;

  }


  private ChunkData getJumpDestLeaves(int id) throws IOException {
    ChunkDataReader dataSetReader = new ChunkDataReader("analysis_jumpdest.json");

    ChunkData result;
    while ((result = dataSetReader.next()) != null) {
      if (result.getId() == id) {
        return result;
      }
    }

    // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
    LOG.error("Contract id {} not found in JumpDest dataset!", id);
    this.failContractIdNotFoundInJumpDestDataSet++;
    return null;
  }

  private ChunkData getFixedLeaves(int id) throws IOException {
    ChunkDataReader dataSetReader = new ChunkDataReader("analysis_fixed.json");

    ChunkData result;
    while ((result = dataSetReader.next()) != null) {
      if (result.getId() == id) {
        return result;
      }
    }

    // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
    LOG.error("Contract id {} not found in Fixed dataset!", id);
    this.failContractIdNotFoundInFixedDataSet++;
    return null;
  }

  private ChunkData getStrictLeaves(int id) throws IOException {
    ChunkDataReader dataSetReader = new ChunkDataReader("analysis_fixed.json");

    ChunkData result;
    while ((result = dataSetReader.next()) != null) {
      if (result.getId() == id) {
        int codeLen = result.getCodeLength();
        int chunkSize = result.getThreshold();
        ArrayList<Integer> chunkStartOffsets = new ArrayList<>();
        for (int ofs = 0; ofs< codeLen; ofs+=chunkSize) {
          chunkStartOffsets.add(ofs);
        }

        ChunkData chunkData = new ChunkData(result.getId(), chunkStartOffsets,
            Bytes.wrap(result.getCode()), result.isStartAddressesAsKeys(), result.getThreshold());
        return chunkData;
      }
    }

    // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
    LOG.error("Contract id {} not found in Fixed dataset!", id);
    this.failContractIdNotFoundInStrictDataSet++;
    return null;
  }


  public void showStats() {
    LOG.info("  functionIdWitnessSize: {} ", this.functionIdWitnessSize);
    LOG.info("  jumpDestWitnessSize: {} ", this.jumpDestWitnessSize);
    LOG.info("  fixedWitnessSize: {} ", this.fixedWitnessSize);

    LOG.info("  failContractIdNotFoundInFunctionIdDataSet: {} ", this.failContractIdNotFoundInFunctionIdDataSet);
    LOG.info("  failContractNotProcessedCorrectlyByFunctionIdAnalysis: {} ", this.failContractNotProcessedCorrectlyByFunctionIdAnalysis);
    LOG.info("  failAllCodeLeafNotFound: {} ", this.failAllCodeLeafNotFound);
    LOG.info("  failContractIdNotFoundInJumpDestDataSet: {} ", this.failContractIdNotFoundInJumpDestDataSet);
    LOG.info("  failContractIdNotFoundInFixedDataSet: {} ", this.failContractIdNotFoundInFixedDataSet);
  }

}
