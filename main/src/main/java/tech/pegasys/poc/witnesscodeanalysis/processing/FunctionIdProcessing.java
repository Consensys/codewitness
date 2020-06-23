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
import tech.pegasys.poc.witnesscodeanalysis.FunctionIdAnalysis;
import tech.pegasys.poc.witnesscodeanalysis.common.SimpleAnalysis;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcess;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessException;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.IOException;
import static org.apache.logging.log4j.LogManager.getLogger;


public class FunctionIdProcessing extends AbstractProcessing {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_NAME =  "functionid";

  private int numSuccessful = 0;
  private int numFailUnknownReason1 = 0;
  private int numFailUnknownReason2 = 0;
  private int numFailInvalidJumpDest = 0;
  private int numFailCodeCopyDynamicParameters = 0;
  private int numFailDynamicJump = 0;
  private int numFailEndFunctionIdBlockNotFound = 0;
  private int numFailCodePathsNotValid = 0;


  public FunctionIdProcessing(boolean json) throws IOException {
    super(DEFAULT_NAME, json);
  }

  @Override
  protected void executeProcessing(int id, String[] deployedAddresses, Bytes code) throws Exception {
    LOG.trace(" Function Id Analysis");

    UnableToProcess unableToProcessInstance = UnableToProcess.getInstance();
    unableToProcessInstance.clean();

    UnableToProcessReason overallResult = null;
    FunctionIdAllResult result =  new FunctionIdAllResult();
    result.setContractInfo(id);

    try {
//    AuxData auxData = new AuxData(code);
      SimpleAnalysis simple = new SimpleAnalysis(code);
      result.setSolidityInfo(simple.isProbablySolidity(), simple.isNewSolidity());


      if (simple.getEndOfFunctionIdBlock() == -1) {
        UnableToProcess.getInstance().unableToProcess(UnableToProcessReason.END_OF_FUNCTION_ID_BLOCK_NOT_FOUND);
      }
      else {
        // Should be able to analyse
        FunctionIdProcess fidAnalysis = new FunctionIdProcess(code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
        fidAnalysis.executeAnalysis(result);

        LOG.trace("  Function Id Process found {} functions", result.getLeaves().size());
        this.numSuccessful++;
        overallResult = UnableToProcessReason.SUCCESS;
      }
    } catch (UnableToProcessException ex) {
      LOG.info(" Unable to Process: {}: {}", unableToProcessInstance.getReason(), unableToProcessInstance.getMessage());
      FunctionIdProcess.addAllCodeLeaf(result, code);

      switch (ex.getReason()) {
        case END_OF_FUNCTION_ID_BLOCK_NOT_FOUND:
          this.numFailEndFunctionIdBlockNotFound++;
          overallResult = UnableToProcessReason.END_OF_FUNCTION_ID_BLOCK_NOT_FOUND;
          break;
        case DYNAMIC_JUMP:
          this.numFailDynamicJump++;
          overallResult = UnableToProcessReason.DYNAMIC_JUMP;
          break;
        case INVALID_JUMP_DEST:
          this.numFailInvalidJumpDest++;
          overallResult = UnableToProcessReason.INVALID_JUMP_DEST;
          break;
        case CODECOPY_WITH_DYNAMIC_PARAMETERS:
          this.numFailCodeCopyDynamicParameters++;
          overallResult = UnableToProcessReason.CODECOPY_WITH_DYNAMIC_PARAMETERS;
          break;
        case CODE_PATHS_NOT_VALID:
          this.numFailCodePathsNotValid++;
          overallResult = UnableToProcessReason.CODE_PATHS_NOT_VALID;
          break;
        default:
          LOG.error("Unknown failure reason: {}", ex.getReason() );
          logStackTrace(ex);
          this.numFailUnknownReason1++;
          overallResult = UnableToProcessReason.UNKNOWN_REASON1;
          break;
      }
    } catch (Throwable th) {
      logStackTrace(th);
      this.numFailUnknownReason2++;
      overallResult = UnableToProcessReason.UNKNOWN_REASON2;
    }
    result.setOverallResult(overallResult);

    if (this.json) {
      gson.toJson(result, this.writer);
      this.writer.append('\n');
    }
    else {
      throw new Error("NOT IMPLEMENTED YET");
    }

  }

  public void showSummary() {
    LOG.info(" {}: Processed: {}, Processed Successfully: {}",
        analysisName.toUpperCase(), this.numberProcessed, this.numSuccessful);
    LOG.info("   Fail(End Of FunctionId Block Not Found): {}, Fail(Dynamic Jump): {}, Fail(Invalid Jump Dest): {}, Fail(Copy Copy Dynamic): {}",
        this.numFailEndFunctionIdBlockNotFound, this.numFailDynamicJump, this.numFailInvalidJumpDest, this.numFailCodeCopyDynamicParameters);
    LOG.info("   Fail(Code Paths Not Valid) {}, Fail(Unknown Reason1): {}, Fail(Processing Failed): {}",
        this.numFailCodePathsNotValid, this.numFailUnknownReason1, this.numFailUnknownReason2);

  }

}
