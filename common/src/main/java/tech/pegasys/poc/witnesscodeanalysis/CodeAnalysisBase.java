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

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.AuxData;
import tech.pegasys.poc.witnesscodeanalysis.common.SimpleAnalysis;

import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  protected Bytes code;
  protected AuxData auxData;
  protected int possibleEndOfCode;
  protected SimpleAnalysis simple;


  public CodeAnalysisBase(Bytes code) {
    this.code = code;
    this.auxData = new AuxData(code);
    this.possibleEndOfCode = code.size() - 1;
    this.simple = new SimpleAnalysis(code);
  }

  public void showBasicInfo() {
    LOG.info("Probably Solidity: {}", this.simple.isProbablySolidity());
    LOG.info("End of Function ID block: 0x{}  ({})", Integer.toHexString(simple.getEndOfFunctionIdBlock()), simple.getEndOfFunctionIdBlock());
    LOG.info("End of Code: 0x{}  ({})", Integer.toHexString(this.simple.getEndOfCode()), this.simple.getEndOfCode());
    LOG.info("Offset Aux Data: 0x{}  ({})", Integer.toHexString(this.auxData.getStartOfAuxData()), this.auxData.getStartOfAuxData());
    LOG.info("Code Length: 0x{}  ({})", Integer.toHexString(this.code.size()), this.code.size());
    if (auxData.hasAuxData()) {
      LOG.info("Compiler {} version {}", auxData.getCompilerName(), auxData.getCompilerVersion());
      LOG.info("Source Code stored in {}, message disgest of source code: {}", auxData.getSourceCodeStorageService(), auxData.getSourceCodeHash());
    }

    if (simple.isProbablySolidity()) {
      LOG.info("Functions found by simple scan");
      Set<Bytes> functionIds = simple.determineFunctionIds(simple.getEndOfCode());
      if (functionIds.size() == 0) {
        LOG.info(" No functions found");
      }
      for (Bytes functionId : functionIds) {
        LOG.info(" Function Id: {}", functionId);
      }
    }
  }

  public String basicInfoAsCsv() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.simple.isProbablySolidity() ? "1," : "0, ");

    buffer.append(this.simple.getEndOfFunctionIdBlock());
    buffer.append(", ");

    buffer.append(this.simple.getEndOfCode());
    buffer.append(", ");

    buffer.append(this.auxData.getStartOfAuxData());
    buffer.append(", ");

    buffer.append(this.code.size());
    buffer.append(", ");

    buffer.append(this.auxData.getCompilerName());
    buffer.append(", ");

    buffer.append(this.auxData.getCompilerVersion());
    buffer.append(", ");

    return buffer.toString();
  }

}
