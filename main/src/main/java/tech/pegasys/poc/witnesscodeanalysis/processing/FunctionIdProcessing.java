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
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllLeaves;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;
import tech.pegasys.poc.witnesscodeanalysis.processing.AbstractProcessing;

import java.io.IOException;
import static org.apache.logging.log4j.LogManager.getLogger;


public class FunctionIdProcessing extends AbstractProcessing {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_NAME =  "functionid";


  public FunctionIdProcessing(boolean json) throws IOException {
    super(DEFAULT_NAME, json);
  }

  @Override
  protected void executeProcessing(Bytes code) throws Exception {
    LOG.trace(" Function Id Analysis");
    AuxData auxData = new AuxData(code);
    SimpleAnalysis simple = new SimpleAnalysis(code, auxData.getStartOfAuxData());

    if (simple.getEndOfFunctionIdBlock() == -1) {
      LOG.trace("  Not attempting Function Id analysis as no function id block was detected");
      // Reduce the number processed successfully to counteract the increment happening in the outer loop.
      this.numberProcessedSuccessfully--;
    }
    else {
      // Should be able to analyse
      FunctionIdProcess fidAnalysis = new FunctionIdProcess(code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
      FunctionIdAllLeaves leaves = fidAnalysis.executeAnalysis();
      LOG.trace("  Function Id Process found {} functions", leaves.getLeaves().size());

      // TODO output JSON or CSV results
    }
  }
}
