package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;
import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessCodeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  public WitnessCodeAnalysis(Bytes code) {
    super(code);
  }

  public void runFunctionIdProcess() {
    FunctionIdProcess analysis = new FunctionIdProcess(this.code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
    analysis.executeAnalysis();
  }


  public static void main(String[] args) throws Exception {
    LOG.info("TODO: add support for contract data.");

    if(args.length != 3) {
      System.out.println("Usage: <application> <input.json> <jumpDestOutput.json> <fixedOutput.json>");
      System.out.println("Current arguments length = " + args.length);
      exit(1);
    }

    //  Parsing the JSON file for contract code
    Gson gson = new GsonBuilder().setLenient().create();

    Reader reader = new FileReader(args[0]);
    Writer jumpDestWriter = new FileWriter(args[1]);
    Writer fixedWriter = new FileWriter(args[2]);
    ContractData[] contractData = gson.fromJson(reader, ContractData[].class);

    for(int i = 0; i < contractData.length; i++) {
      LOG.info("***********REACHED**********");
      Bytes code = Bytes.fromHexString(contractData[i].getCode());

      // Analysis of jumpdests
      LOG.info("\nJumpDest Analysis started");
      ArrayList<Integer> chunkStartAddresses = new JumpDestAnalysis().analyse(128, code);
      LOG.info("\nFinished. {} chunks", chunkStartAddresses.size());
      ChunkData chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, jumpDestWriter);

      // Analysis doing fixed size chunking
      LOG.info("\nFixedSize Analysis started");
      chunkStartAddresses = new FixedSizeAnalysis().analyse(128, code);
      LOG.info("\nFinishe. {} chunks.", chunkStartAddresses.size());
      chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, fixedWriter);

      // Function ID analysis
      WitnessCodeAnalysis analysis = new WitnessCodeAnalysis(code);
      analysis.showBasicInfo();
      analysis.runFunctionIdProcess();
    }

    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }


}
