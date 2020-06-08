package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractData;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllLeaves;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
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

  private static final int THRESHOLD = 128;

  public WitnessCodeAnalysis(Bytes code) {
    super(code);
  }

  public static void main(String[] args) throws Exception {
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
    int successfullyAnalysed = 0;



    String line = reader.readLine();
    int i = 0;
    // loop until all lines are read
    while (line != null && i < 10000) {
      i++;
      // LOG.info(line);
      ContractData contractData = gson.fromJson(line, ContractData.class);
      String contractAddress = contractData.getContract_address()[0];
      int numDeployments = contractData.getContract_address().length;

      // Print out information about the contract to get a feel for how important the results are.
      LOG.info("Processing contract {} deployed at address: {} and {} other times", i, contractAddress, numDeployments-1);
      Bytes code = Bytes.fromHexString(contractData.getCode());
      LOG.info(" Code Size: " + code.size());
      int firstDeployment = 100000000;
      int[] deployments = contractData.getDeployed_at_block();
      for (int j = 0; j < numDeployments; j++) {
        if (deployments[j] < firstDeployment) {
          firstDeployment = deployments[j];
        }
      }
      LOG.info(" Deployed at block: " + contractData.getDeployed_at_block()[0]);
      int lastTransaction = 0;
      int[] lastTransactions = contractData.getRecent_accessed_at_block();
      for (int j = 0; j < numDeployments; j++) {
        if (lastTransactions[j] > lastTransaction) {
          lastTransaction = lastTransactions[j];
        }
      }
      LOG.info(" Last transaction for all deployments: {}", lastTransaction);

      ArrayList<Integer> chunkStartAddresses;
      ChunkData chunkData;

      // Analysis of jumpdests
      LOG.info(" JumpDest Analysis started");
      try {
        chunkStartAddresses = new JumpDestAnalysis(code, THRESHOLD).analyse();
        LOG.info("  Finished. {} chunks", chunkStartAddresses.size());
        chunkData = new ChunkData(chunkStartAddresses, code, true, THRESHOLD);
        gson.toJson(chunkData, jumpDestWriter);
      } catch (Throwable th) {
        logStackTrace(th);
      }


      // Analysis doing fixed size chunking
      LOG.info(" FixedSize Analysis started");
      try {
        chunkStartAddresses = new FixedSizeAnalysis(code, THRESHOLD).analyse();
        LOG.info("  Finished. {} chunks.", chunkStartAddresses.size());
        chunkData = new ChunkData(chunkStartAddresses, code, true, THRESHOLD);
        gson.toJson(chunkData, fixedWriter);
      } catch (Throwable th) {
        logStackTrace(th);
      }

      // Analysis doing strict fixed size chunking
      LOG.info(" StrictFixedSize Analysis started");
      try {
        ArrayList<Integer> chunkStartOffsets = new StrictFixedSizeAnalysis(code, THRESHOLD).analyse();
        LOG.info("  Finished. {} chunks.", chunkStartOffsets.size());
        chunkData = new ChunkData(chunkStartOffsets, code, false, THRESHOLD);
        gson.toJson(chunkData, fixedWriter);
      } catch (Throwable th) {
        logStackTrace(th);
      }

      // Function ID analysis
      WitnessCodeAnalysis analysis = new WitnessCodeAnalysis(code);
      //analysis.showBasicInfo();

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

      LOG.info(" Function Id Analysis");
      if (analysis.simple.getEndOfFunctionIdBlock() == -1) {
        LOG.info("  Not attempting Function Id analysis as no function id block was detected");
      }
      else {
        // Should be able to analyse
        try {
          FunctionIdProcess fidAnalysis = new FunctionIdProcess(code, analysis.simple.getEndOfFunctionIdBlock(), analysis.simple.getEndOfCode(), analysis.simple.getJumpDests());
          FunctionIdAllLeaves leaves = fidAnalysis.executeAnalysis();
          LOG.info("  Function Id Process found {} functions", leaves.getLeaves().size());
          successfullyAnalysed++;
        } catch (Throwable th) {
          logStackTrace(th);
        }
      }

      // read next line before looping
      //if end of file reached, line would be null
      line = reader.readLine();
    }


    LOG.info("Total: {}, Probably Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, Successfully Analysed: {}, End Of Code Found: {}",
        total, numSol, definitelySol, shouldBeAbleToAnalyse, successfullyAnalysed, simpleAnalysisCompleted);


    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }


  private static void logStackTrace(Throwable th) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    LOG.info("  Exception while processing: {}", sw.toString());

  }

}
