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
package tech.pegasys.poc.witnesscodeanalysis.processing;

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
import java.io.IOException;
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

abstract class AbstractProcessing {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_BASE_FILE_NAME =  "analysis_";
  public static final String JSON =  ".json";
  public static final String CSV =  ".csv";

  public static final int THRESHOLD = 128;


  Writer writer;
  boolean json;
  String analysisName;

  Gson gson;

  int numberProcessed = 0;
  int numberProcessedSuccessfully = 0;


  public AbstractProcessing(String analysisName, boolean json) throws IOException {
    String outputFileNameBase = DEFAULT_BASE_FILE_NAME + analysisName;
    String outputFileName = json ? outputFileNameBase + JSON : outputFileNameBase + CSV;
    this.json = json;
    this.writer = new FileWriter(outputFileName);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();

    this.analysisName = analysisName;
  }

  public boolean process(Bytes code) {
    try {
      this.numberProcessed++;
      executeProcessing(code);
      this.numberProcessedSuccessfully++;
      return true;
    } catch (Throwable th) {
      logStackTrace(th);
      return false;
    }
  }

  protected abstract void executeProcessing(Bytes code) throws Exception;

  public void close() throws IOException {
    writer.close();
  }

  public int getNumberProcessed() {
    return numberProcessed;
  }

  public int getNumberProcessedSuccessfully() {
    return numberProcessedSuccessfully;
  }

  public void showSummary() {
    LOG.info(" {}: Processed: {}, Processed Successfully: {}",
        analysisName.toUpperCase(), this.numberProcessed, this.numberProcessedSuccessfully);
  }

  protected static void logStackTrace(Throwable th) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    LOG.info("  Exception while processing: {}", sw.toString());

  }


}
