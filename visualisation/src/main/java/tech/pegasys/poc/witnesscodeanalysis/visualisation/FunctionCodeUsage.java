package tech.pegasys.poc.witnesscodeanalysis.visualisation;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdMerklePatriciaTrieLeafData;

import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FunctionCodeUsage {
  private static final Logger LOG = getLogger();
  Bytes allCodeFunctionId = Bytes.wrap(new byte[]{1, 0, 0, 0, 0});

  FunctionIdDataSetReader reader;

  public FunctionCodeUsage() throws Exception {
    this.reader = new FunctionIdDataSetReader();
  }

  public void run() throws Exception {
    FunctionIdAllResult allLeaves = this.reader.next();
    // Assume leaves aren't null

    int codeSize = 0;
    ArrayList<FunctionIdMerklePatriciaTrieLeafData> leaves = allLeaves.getLeaves();
    for (FunctionIdMerklePatriciaTrieLeafData leaf: leaves) {
      int totalLength = 0;
      Bytes functionId = leaf.getFunctionId();
      LOG.info("FunctionId: {}", functionId);
      BasicBlockWithCode[] blocks = leaf.getBasicBlocksWithCode();
      for (BasicBlockWithCode block: blocks) {
        LOG.info(" Block start: {}, len: {}", block.getStart(), block.getLength());
        totalLength += block.getLength();
      }
      LOG.info(" Total length {} bytes", totalLength);

      if (functionId.compareTo(this.allCodeFunctionId) == 0) {
        codeSize = blocks[0].getLength();
      }
    }

    char used = 'X';
    char notUsed = '.';
    int columns = 80;
    int rows = 10;
    int volume = columns * rows;
    int quantizationStep = 0;
    if (volume >= codeSize) {
      quantizationStep = 1;
    }
    else {
      quantizationStep = codeSize / volume + 1;
    }
    System.out.println("Quantum Step: " + quantizationStep);
    boolean[] inUse = new boolean[codeSize/quantizationStep];
    boolean[] notUsedEver = new boolean[codeSize/quantizationStep];
    Arrays.fill(notUsedEver, true);

    leaves = allLeaves.getLeaves();
    for (FunctionIdMerklePatriciaTrieLeafData leaf: leaves) {
      Arrays.fill(inUse, false);

      Bytes functionId = leaf.getFunctionId();
      LOG.info("FunctionId: {}", functionId);
      BasicBlockWithCode[] blocks = leaf.getBasicBlocksWithCode();
      for (BasicBlockWithCode block: blocks) {
        for (int i = block.getStart(); i < block.getStart() + block.getLength(); i++) {
          inUse[i/quantizationStep] = true;
          if (functionId.compareTo(this.allCodeFunctionId) != 0) {
            notUsedEver[i/quantizationStep] = false;
          }
        }
      }

      for (int i = 0; i < inUse.length; i++) {
        System.out.print(inUse[i] ? used : notUsed);
        if ((i+1) % columns == 0) {
          System.out.println();
        }
      }
      System.out.println();
    }

    System.out.println("Not used");
    for (int i = 0; i < inUse.length; i++) {
      System.out.print(notUsedEver[i] ? used : notUsed);
      if ((i+1) % columns == 0) {
        System.out.println();
      }
    }
    System.out.println();


  }



  public static void main(String[] args) throws Exception {
    (new FunctionCodeUsage()).run();


  }

}
