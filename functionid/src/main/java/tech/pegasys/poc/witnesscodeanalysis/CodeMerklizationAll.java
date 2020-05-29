package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractInfo;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractJsonProcessor;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodeMerklizationAll {
  private static final Logger LOG = getLogger();

  public static void main(String[] args) throws Exception {
    String filein =  "contract_data.json";
    String fileout =  "codemerk.csv";


    Path pathToFileIn = Paths.get(filein);
    Path pathToFileOut = Paths.get(fileout);

    BufferedWriter bw = Files.newBufferedWriter(pathToFileOut, StandardCharsets.US_ASCII);
    BufferedReader br = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);

    ContractJsonProcessor inputProcessor = new ContractJsonProcessor();

    int numSol = 0;
    int total = 0;
    int definitelySol = 0;
    int simpleAnalysisCompleted = 0;
    int shouldBeAbleToAnalyse = 0;


    // read the first line from the text file
    String line = br.readLine();
    // loop until all lines are read
    while (line != null) {
      // LOG.info(line);

      ContractInfo contractInfo = inputProcessor.processEntry(line);

      CodeAnalysisBase analysis = new CodeAnalysisBase(contractInfo.getCode());
      // LOG.info("Analysis: {}", analysis.basicInfoAsCsv());


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
//        try {
//          FunctionIdProcess fidAnalysis = new FunctionIdProcess(contractInfo.getCode(), analysis.simple.getEndOfFunctionIdBlock(), analysis.simple.getEndOfCode());
//          fidAnalysis.executeAnalysis();
//        } catch (Throwable th) {
//          th.printStackTrace();
//        }
      }

      // read next line before looping
      //if end of file reached, line would be null
      line = br.readLine();
    }
    br.close();
    bw.close();

    LOG.info("Total: {}, Probably Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, End Of Code Found: {}", total, numSol, definitelySol, shouldBeAbleToAnalyse, simpleAnalysisCompleted);

  }

}
