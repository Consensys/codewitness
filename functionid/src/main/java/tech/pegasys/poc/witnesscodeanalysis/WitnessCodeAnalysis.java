package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractData;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static java.lang.System.exit;
import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessCodeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_FILE_IN =  "contract_data.json";
  public static final String DEFAULT_JUMPDEST_FILE_OUT =  "analysis_jumpdest.json";
  public static final String DEFAULT_FIXED_FILE_OUT =  "analysis_fixed.json";

  public WitnessCodeAnalysis(Bytes code) {
    super(code);
  }

  public static void main(String[] args) throws Exception {
    LOG.info("TODO: add support for contract data.");

    String fileIn = null;
    String jumpDestFileOut = null;
    String fixedFileOut = null;
    if (args.length == 0) {
      fileIn = DEFAULT_FILE_IN;
      jumpDestFileOut = DEFAULT_JUMPDEST_FILE_OUT;
      fixedFileOut = DEFAULT_FIXED_FILE_OUT;
    }
    else if (args.length == 3) {
      fileIn = args[0];
      jumpDestFileOut = args[1];
      fixedFileOut = args[2];
    }
    else {
      System.out.println("Usage: <application> <input.json> <jumpDestOutput.json> <fixedOutput.json>");
      System.out.println("Current arguments length = " + args.length);
      exit(1);
    }

    //  Parsing the JSON file for contract code
    Gson gson = new GsonBuilder().setLenient().create();

    Path pathToFileIn = Paths.get(fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    Writer jumpDestWriter = new FileWriter(jumpDestFileOut);
    Writer fixedWriter = new FileWriter(fixedFileOut);

    int numSol = 0;
    int total = 0;
    int definitelySol = 0;
    int simpleAnalysisCompleted = 0;
    int shouldBeAbleToAnalyse = 0;



    String line = reader.readLine();
    int i = 0;
    // loop until all lines are read
    while (line != null && i < 6) {
      i++;
      // LOG.info(line);
      ContractData contractData = gson.fromJson(line, ContractData.class);
      LOG.info("Processing contract at address: {}", contractData.getContract_address()[0]);
      Bytes code = Bytes.fromHexString(contractData.getCode());

      // Analysis of jumpdests
      LOG.info("\nJumpDest Analysis started");
      ArrayList<Integer> chunkStartAddresses = new JumpDestAnalysis(code, 128).analyse();
      LOG.info("\nFinished. {} chunks", chunkStartAddresses.size());
      ChunkData chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, jumpDestWriter);

      // Analysis doing fixed size chunking
      LOG.info("\nFixedSize Analysis started");
      chunkStartAddresses = new FixedSizeAnalysis(code, 128).analyse();
      LOG.info("\nFinished. {} chunks.", chunkStartAddresses.size());
      chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, fixedWriter);

      // Function ID analysis
      WitnessCodeAnalysis analysis = new WitnessCodeAnalysis(code);
      analysis.showBasicInfo();

      total++;
      if (analysis.simple.isProbablySolidity()) {
        numSol++;
      }

      // There is aux data and it indicates solc is the compiler.
      if (analysis.auxData.isDefinitelySolidity()) {
        definitelySol++;
      }

      if (analysis.simple.getEndOfFunctionIdBlock() != -1) {
        shouldBeAbleToAnalyse++;
      }

      if (analysis.simple.simpleAnalysisCompleted()) {
        simpleAnalysisCompleted++;
      }

      // Should be able to analyse
      if (analysis.simple.getEndOfFunctionIdBlock() != -1) {
        try {
          FunctionIdProcess fidAnalysis = new FunctionIdProcess(code, analysis.simple.getEndOfFunctionIdBlock(), analysis.simple.getEndOfCode(), analysis.simple.getJumpDests());
          fidAnalysis.executeAnalysis();
        } catch (Throwable th) {
          th.printStackTrace();
        }
      }

      // read next line before looping
      //if end of file reached, line would be null
      line = reader.readLine();
    }


    LOG.info("Total: {}, Probably Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, End Of Code Found: {}", total, numSol, definitelySol, shouldBeAbleToAnalyse, simpleAnalysisCompleted);


    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }


}
