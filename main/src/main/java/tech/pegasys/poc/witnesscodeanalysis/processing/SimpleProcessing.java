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

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.AuxData;
import tech.pegasys.poc.witnesscodeanalysis.common.SimpleAnalysis;

import java.io.IOException;

import static org.apache.logging.log4j.LogManager.getLogger;

public class SimpleProcessing extends AbstractProcessing {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_NAME =  "simple";

  int numSol = 0;
  int newSol = 0;
  int definitelySol = 0;
  int shouldBeAbleToAnalyse = 0;
  int endOfCodeDetected = 0;


  public SimpleProcessing(boolean json) throws IOException {
    super(DEFAULT_NAME, json);
  }

  @Override
  protected void executeProcessing(int id, String[] deployedAddresses, Bytes code) throws Exception {
    AuxData auxData = new AuxData(code);
    SimpleAnalysis simple = new SimpleAnalysis(code);
    //analysis.showBasicInfo();

    if (simple.isProbablySolidity()) {
      numSol++;
    }

    if (simple.isNewSolidity()) {
      this.newSol++;
    }

    // There is aux data and it indicates solc is the compiler.
    if (auxData.isDefinitelySolidity()) {
      definitelySol++;
    }

    if (simple.getEndOfFunctionIdBlock() != -1) {
      shouldBeAbleToAnalyse++;
    }

    if (simple.endOfCodeDetected()) {
      this.endOfCodeDetected++;
    }
  }


  public int getNumSol() {
    return numSol;
  }

  public int getDefinitelySol() {
    return definitelySol;
  }

  public int getShouldBeAbleToAnalyse() {
    return shouldBeAbleToAnalyse;
  }

  @Override
  public void showSummary() {
    super.showSummary();
    LOG.info("  Probably Solidity: {}, New Solidity: {}, Definitely Solidity: {}, Should be able to Analyse: {}, End Of Code Detected: {}",
        numSol, this.newSol, this.definitelySol, this.shouldBeAbleToAnalyse, this.endOfCodeDetected);
  }

}
