package tech.pegasys.poc.witnesscodeanalysis.combined;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.MainNetContractDataSet;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkData;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkDataReader;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.functionid.CodeVisitor;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdMerklePatriciaTrieLeafData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
    FunctionIdAllResult result = functionIdResult(id);
    if (result == null) {
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

    // TODO this is a bug - will count multiple times, for each time the same leaf is used.
    // Do part of the function id analysis here.
    this.functionIdRawLeafSizeWithoutCode += codeBlocks.length * 4 + 4;
    this.functionIdNumberOfLeaves++;
  }

  public void calculateLeafPlusCodeSizes() throws IOException {
    LOG.info("calculateLeafPlusCodeSizes");

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
      Set<Integer> chunksUsed = determineChunksUsed(codeBlocks, startOffsetsAndLengths );
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
      Set<Integer> chunksUsed = determineChunksUsed(codeBlocks, startOffsetsAndLengths );
      for (Integer startUsed: chunksUsed) {
        fixedDataUsed += startOffsetsAndLengths.get(startUsed);
      }
    }
    this.fixedWitnessSize = fixedDataUsed;

    int strictDataUsed = 0;
    for (Integer id : this.contractCodeExecuted.keySet()) {
      Map<Integer, Integer> codeBlocks = this.contractCodeExecuted.get(id);
      ChunkData chunkData = getStrictLeaves(id);
      if (chunkData != null) {
        Map<Integer, Integer> startOffsetsAndLengths = chunkData.getChunks();
        Set<Integer> chunksUsed = determineChunksUsed(codeBlocks, startOffsetsAndLengths );
        for (Integer startUsed: chunksUsed) {
          if (startUsed == null) {
            LOG.error("WHY IS STARTUSED NULL? Id: {}", id);
          }
          else {
            try {
              strictDataUsed += startOffsetsAndLengths.get(startUsed);
            } catch (NullPointerException ex) {
              LOG.error("WHERE IS NULL POINTER EXCEPTION id: {}", id);
            }
          }
        }
      }
    }
    this.strictWitnessSize = strictDataUsed;
  }


  private static Map<Integer, ChunkData> jumpDestEverything = new HashMap<>();
  private ChunkData getJumpDestLeaves(int id) throws IOException {
    if (jumpDestEverything.isEmpty()) {
      LOG.info("Loading analysis_jumpdest.json");
      ChunkDataReader dataSetReader = new ChunkDataReader("analysis_jumpdest.json");
      ChunkData result;
      while ((result = dataSetReader.next()) != null) {
        jumpDestEverything.put(result.getId(), result);
      }
    }

    ChunkData chunkData = jumpDestEverything.get(id);
    if (chunkData == null) {
      // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
      LOG.error("Contract id {} not found in JumpDest dataset!", id);
      this.failContractIdNotFoundInJumpDestDataSet++;
      return null;
    }
    return chunkData;
  }

  private static Map<Integer, ChunkData> fixedEverything = new HashMap<>();
  private ChunkData getFixedLeaves(int id) throws IOException {
    if (fixedEverything.isEmpty()) {
      LOG.info("Loading analysis_fixed.json");
      ChunkDataReader dataSetReader = new ChunkDataReader("analysis_fixed.json");
      ChunkData result;
      while ((result = dataSetReader.next()) != null) {
        fixedEverything.put(result.getId(), result);
      }
    }

    ChunkData chunkData = fixedEverything.get(id);
    if (chunkData == null) {
      // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
      LOG.error("Contract id {} not found in Fixed dataset!", id);
      this.failContractIdNotFoundInFixedDataSet++;
      return null;
    }
    return chunkData;
  }

  private static Map<Integer, ChunkData> strictEverything = new HashMap<>();
  private ChunkData getStrictLeaves(int id) throws IOException {
    if (strictEverything.isEmpty()) {
      LOG.info("Loading analysis_strict.json");
      ChunkDataReader dataSetReader = new ChunkDataReader("analysis_strictfixed.json");
      ChunkData result;
      while ((result = dataSetReader.next()) != null) {
        int codeLen = result.getCodeLength();
        int chunkSize = result.getThreshold();
        ArrayList<Integer> chunkStartOffsets = new ArrayList<>();
        for (int ofs = 0; ofs< codeLen; ofs+=chunkSize) {
          chunkStartOffsets.add(ofs);
        }

        ChunkData chunkData1 = new ChunkData(result.getId(), chunkStartOffsets,
            Bytes.wrap(new byte[codeLen]), result.isStartAddressesAsKeys(), result.getThreshold());
        strictEverything.put(result.getId(), chunkData1);
      }
    }

    ChunkData chunkData = strictEverything.get(id);
    if (chunkData == null) {
      // TODO consider throwing an error. Arriving here would imply the contract is in the function id data set, but not his one.
      LOG.error("Contract id {} not found in Fixed dataset!", id);
      this.failContractIdNotFoundInStrictDataSet++;
      return null;
    }
    return chunkData;
  }


  private Set<Integer> determineChunksUsed(Map<Integer, Integer> codeBlocks, Map<Integer, Integer> startOffsetsAndLengths) {
    Set<Integer> chunksUsed = new TreeSet<>();
    for (Integer start: codeBlocks.keySet()) {
      int length = codeBlocks.get(start);
      // Determine the chunks needed for this code block.
      int lastChunkStart = 0;
      int chunkStart = 0;
      Iterator<Integer> chunkStartIter = startOffsetsAndLengths.keySet().iterator();

      // Iterate to the start of the chunk.
      while (chunkStartIter.hasNext() && chunkStart < start) {
        lastChunkStart = chunkStart;
        chunkStart = chunkStartIter.next();
      }

      // If the function id chunk is after the start of the last chunk
      if (!chunkStartIter.hasNext()) {
        chunksUsed.add(chunkStart);
      }
      else {
        if (chunkStart == start) {
          chunksUsed.add(chunkStart);
        }
        else {
          // Chunk start must be greater than
          chunksUsed.add(lastChunkStart);
        }

        // Add all the chunks that are within the function id chunk
        while (chunkStartIter.hasNext() && chunkStart < start+length) {
          chunksUsed.add(chunkStart);
          chunkStart = chunkStartIter.next();
        }
      }
    }

    return chunksUsed;
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
    LOG.info("  functionIdWitnessSize: {} ", this.functionIdWitnessSize);
    LOG.info("  jumpDestWitnessSize: {} ", this.jumpDestWitnessSize);
    LOG.info("  fixedWitnessSize: {} ", this.fixedWitnessSize);
    LOG.info("  strictWitnessSize: {} ", this.strictWitnessSize);

    LOG.info("  failContractIdNotFoundInFunctionIdDataSet: {} ", this.failContractIdNotFoundInFunctionIdDataSet);
    LOG.info("  failContractNotProcessedCorrectlyByFunctionIdAnalysis: {} ", this.failContractNotProcessedCorrectlyByFunctionIdAnalysis);
    LOG.info("  failAllCodeLeafNotFound: {} ", this.failAllCodeLeafNotFound);
    LOG.info("  failContractIdNotFoundInJumpDestDataSet: {} ", this.failContractIdNotFoundInJumpDestDataSet);
    LOG.info("  failContractIdNotFoundInFixedDataSet: {} ", this.failContractIdNotFoundInFixedDataSet);
    LOG.info("  failContractIdNotFoundInStrictDataSet: {} ", this.failContractIdNotFoundInStrictDataSet);
  }

  public void setResultInformation(WitnessResult result) {
    result.functionIdWitnessSize = this.functionIdWitnessSize;
    result.jumpDestWitnessSize = this.jumpDestWitnessSize;
    result.fixedWitnessSize = fixedWitnessSize;
    result.strictWitnessSize = strictWitnessSize;
  }

}
