/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.AuxData;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkData;
import tech.pegasys.poc.witnesscodeanalysis.common.ContractData;
import tech.pegasys.poc.witnesscodeanalysis.common.SimpleAnalysis;
import tech.pegasys.poc.witnesscodeanalysis.fixed.FixedSizeAnalysis;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllLeaves;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;
import tech.pegasys.poc.witnesscodeanalysis.jumpdest.JumpDestAnalysis;

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

public class CodeAnalysisProcess {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_FILE_IN =  "contract_data.json";
  public static final String DEFAULT_JUMPDEST_FILE_OUT =  "analysis_jumpdest.json";
  public static final String DEFAULT_FIXED_FILE_OUT =  "analysis_fixed.json";

  BufferedReader reader;
  Writer jumpDestWriter;
  Writer fixedWriter;

  Gson gson;
  Bytes code;

  int contractNumber = 0;

  int numSol = 0;
  int total = 0;
  int definitelySol = 0;
  int simpleAnalysisCompleted = 0;
  int shouldBeAbleToAnalyse = 0;
  int successfullyAnalysed = 0;


  public CodeAnalysisProcess(BufferedReader reader, Writer jumpDestWriter, Writer fixedWriter) {
    this.reader = reader;
    this.jumpDestWriter = jumpDestWriter;
    this.fixedWriter = fixedWriter;

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();

  }

  public void analyseAll() throws Exception {
    String line = reader.readLine();
    // loop until all lines are read
    while (line != null) {
      analyse1(line);

      // read next line before looping
      //if end of file reached, line would be null
      line = reader.readLine();

      this.contractNumber++;
    }
    LOG.info("Total: {}, Probably Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, Successfully Analysed: {}, End Of Code Found: {}",
        total, numSol, definitelySol, shouldBeAbleToAnalyse, successfullyAnalysed, simpleAnalysisCompleted);

    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }

  public void analyseUpTo(int limit) throws Exception {
    String line = reader.readLine();
    // loop until all lines are read
    while (line != null) {
      analyse1(line);

      // read next line before looping
      //if end of file reached, line would be null
      line = reader.readLine();

      this.contractNumber++;
      if (this.contractNumber == limit) {
        break;
      }
    }
    LOG.info("Total: {}, Probably Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, Successfully Analysed: {}, End Of Code Found: {}",
        total, numSol, definitelySol, shouldBeAbleToAnalyse, successfullyAnalysed, simpleAnalysisCompleted);

    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }


  public void analyseOne(int targetContractNumber) throws Exception {
    String line = reader.readLine();

    // loop until all lines are read
    while (line != null) {
      if (this.contractNumber == targetContractNumber) {
        analyse(line);
        break;
      }
      else {
        this.contractNumber++;
      }

      // read next line before looping
      //if end of file reached, line would be null
      line = reader.readLine();
    }

    reader.close();
    jumpDestWriter.close();
    fixedWriter.close();
  }


  private void analyse(String line) {
    loadContract(line, true);

    ArrayList<Integer> chunkStartAddresses;
    ChunkData chunkData;

    // Analysis of jumpdests
    LOG.info(" JumpDest Analysis started");
    try {
      chunkStartAddresses = new JumpDestAnalysis(code, 128).analyse();
      LOG.info("  Finished. {} chunks", chunkStartAddresses.size());
      chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, jumpDestWriter);
    } catch (Throwable th) {
      logStackTrace(th);
    }


    // Analysis doing fixed size chunking
    LOG.info(" FixedSize Analysis started");
    try {
      chunkStartAddresses = new FixedSizeAnalysis(code, 128).analyse();
      LOG.info("  Finished. {} chunks.", chunkStartAddresses.size());
      chunkData = new ChunkData(chunkStartAddresses);
      gson.toJson(chunkData, fixedWriter);
    } catch (Throwable th) {
      logStackTrace(th);
    }


    // Function ID analysis
    AuxData auxData = new AuxData(code);
    SimpleAnalysis simple = new SimpleAnalysis(code, auxData.getStartOfAuxData());
    //analysis.showBasicInfo();

    total++;
    if (simple.isProbablySolidity()) {
      numSol++;
    }

    // There is aux data and it indicates solc is the compiler.
    if (auxData.isDefinitelySolidity()) {
      definitelySol++;
    }

    if (simple.getEndOfFunctionIdBlock() != -1) {
      shouldBeAbleToAnalyse++;
    }

    if (simple.simpleAnalysisCompleted()) {
      simpleAnalysisCompleted++;
    }

    LOG.info(" Function Id Analysis");
    if (simple.getEndOfFunctionIdBlock() == -1) {
      LOG.info("  Not attempting Function Id analysis as no function id block was detected");
    }
    else {
      // Should be able to analyse
      try {
        FunctionIdProcess fidAnalysis = new FunctionIdProcess(code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
        FunctionIdAllLeaves leaves = fidAnalysis.executeAnalysis();
        LOG.info("  Function Id Process found {} functions", leaves.getLeaves().size());
        successfullyAnalysed++;
      } catch (Throwable th) {
        logStackTrace(th);
      }
    }

  }

  private void analyse1(String line) {
    loadContract(line, false);
    analyseSimple(line);
  }


  private void loadContract(String line, boolean showInfo) {
    ContractData contractData = gson.fromJson(line, ContractData.class);
    this.code = Bytes.fromHexString(contractData.getCode());

    if (showInfo) {
      String contractAddress = contractData.getContract_address()[0];
      int numDeployments = contractData.getContract_address().length;

      // Print out information about the contract to get a feel for how important the results are.
      LOG.info("Processing contract {} deployed at address: {} and {} other times", this.contractNumber, contractAddress, numDeployments - 1);
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
      if (lastTransactions != null) {
        for (int j = 0; j < numDeployments; j++) {
          if (lastTransactions[j] > lastTransaction) {
            lastTransaction = lastTransactions[j];
          }
        }
        LOG.info(" Last transaction for all deployments: {}", lastTransaction);
      }
    }
  }


  private void analyseSimple(String line) {
    //LOG.info(" Deployed at block: " + contractData.getDeployed_at_block()[0]);
    // Function ID analysis
    AuxData auxData = new AuxData(code);
    SimpleAnalysis simple = new SimpleAnalysis(code, auxData.getStartOfAuxData());
    //analysis.showBasicInfo();

    total++;
    if (simple.isProbablySolidity()) {
      numSol++;
    }

    // There is aux data and it indicates solc is the compiler.
    if (auxData.isDefinitelySolidity()) {
      definitelySol++;
    }

    if (simple.getEndOfFunctionIdBlock() != -1) {
      shouldBeAbleToAnalyse++;
    }

    if (simple.simpleAnalysisCompleted()) {
      simpleAnalysisCompleted++;
    }
  }

  private static void logStackTrace(Throwable th) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    LOG.info("  Exception while processing: {}", sw.toString());

  }

  public static void main(String[] args) throws Exception {
    String fileIn = null;
    String jumpDestFileOut = null;
    String fixedFileOut = null;
    if (args.length == 0) {
      fileIn = DEFAULT_FILE_IN;
      jumpDestFileOut = DEFAULT_JUMPDEST_FILE_OUT;
      fixedFileOut = DEFAULT_FIXED_FILE_OUT;
    } else if (args.length == 3) {
      fileIn = args[0];
      jumpDestFileOut = args[1];
      fixedFileOut = args[2];
    } else {
      System.out.println("Usage: <application> <input.json> <jumpDestOutput.json> <fixedOutput.json>");
      System.out.println("Current arguments length = " + args.length);
      exit(1);
    }

    Path pathToFileIn = Paths.get(fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    Writer jumpDestWriter = new FileWriter(jumpDestFileOut);
    Writer fixedWriter = new FileWriter(fixedFileOut);

    CodeAnalysisProcess witnessCodeAnalysis = new CodeAnalysisProcess(reader, jumpDestWriter, fixedWriter);
    witnessCodeAnalysis.analyseAll();
  }
}
