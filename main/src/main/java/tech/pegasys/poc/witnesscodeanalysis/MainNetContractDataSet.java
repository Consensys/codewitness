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
import tech.pegasys.poc.witnesscodeanalysis.common.ContractData;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.logging.log4j.LogManager.getLogger;

public class MainNetContractDataSet {
//  private static final Logger LOG = getLogger();

  public static final String DEFAULT_FILE_IN =  "contract_data.json";
  private BufferedReader reader;
  private Gson gson;


  public MainNetContractDataSet() throws IOException {
    this(DEFAULT_FILE_IN);
  }

  public MainNetContractDataSet(String fileIn) throws IOException {
    Path pathToFileIn = Paths.get(fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    this.reader = reader;

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public ContractData next() throws Exception {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return gson.fromJson(line, ContractData.class);
    } catch (IOException ioe) {
      return null;
    }
  }

  public void close() throws IOException {
    reader.close();
  }
}
