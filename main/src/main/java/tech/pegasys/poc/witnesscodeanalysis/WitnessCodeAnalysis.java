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
import tech.pegasys.poc.witnesscodeanalysis.bytecodedump.ByteCodeDump;
import tech.pegasys.poc.witnesscodeanalysis.common.ContractData;
import tech.pegasys.poc.witnesscodeanalysis.processing.FunctionIdProcessing;
import tech.pegasys.poc.witnesscodeanalysis.processing.SimpleProcessing;

import java.io.IOException;

import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessCodeAnalysis {
  private static final Logger LOG = getLogger();

  public static boolean SIMPLE = true;
  public static boolean FUNCTIONID = true;

  private final MainNetContractDataSet dataSet;
  private final SimpleProcessing simpleProcessing;
  private final FunctionIdProcessing functionIdProcessing;


  public WitnessCodeAnalysis() throws IOException {
    this.dataSet = new MainNetContractDataSet();
    this.simpleProcessing = new SimpleProcessing(true);
    this.functionIdProcessing = new FunctionIdProcessing(true);
  }

  public void analyseAll() throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      contractData.showInfo(count);
      process(count, contractData);
      count++;

      if (count % 1000 == 0) {
        LOG.info(count);
      }
    }
    closeAll();
  }

  public void analyseUpTo(int limit) throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      contractData.showInfo(count);
      process(count, contractData);
      count++;
      if (count == limit) {
        break;
      }
    }
    closeAll();
  }

  public void analyseOne(int theOne) throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      if (count == theOne) {
        contractData.showInfo(count);
        process(count, contractData);
        break;
      }
      count++;
    }
    closeAll();
  }

  public void analyseDeployedBlockNumbers(int start, int end) throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      boolean analyse = false;
      int[] deployedBlockNumbers = contractData.getDeployed_at_block();
      for (int deployedBlockNumber: deployedBlockNumbers) {
        if (deployedBlockNumber >= start && deployedBlockNumber <= end) {
          analyse = true;
          break;
        }
      }

      if (analyse) {
        contractData.showInfo(count);
        process(count, contractData);
      }
      count++;
    }
    closeAll();
  }


  public void dumpOne(int theOne) throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      if (count == theOne) {
        contractData.showInfo(count);
        Bytes code = Bytes.fromHexString(contractData.getCode());
        ByteCodeDump dump = new ByteCodeDump(code);
        try {
          dump.showBasicInfo();
        } catch (Throwable ex) {
          System.out.println(ex);
        }
        dump.dumpContract();
        break;
      }
      count++;
    }
    closeAll();
  }


  public void process(int id, ContractData contractData) {
    Bytes code = Bytes.fromHexString(contractData.getCode());

    if (SIMPLE) {
      this.simpleProcessing.process(id, contractData.getContract_address(), code);
    }

    if (FUNCTIONID) {
      this.functionIdProcessing.process(id, contractData.getContract_address(), code);
    }
  }

  public void showSummary() {
    LOG.info("Summary");
    if (SIMPLE) {
      this.simpleProcessing.showSummary();
    }

    if (FUNCTIONID) {
      this.functionIdProcessing.showSummary();
    }


  }

  private void closeAll() throws IOException {
    this.dataSet.close();
    this.simpleProcessing.close();
    this.functionIdProcessing.close();
  }




  public static void main(String[] args) throws Exception {
    WitnessCodeAnalysis witnessCodeAnalysis = new WitnessCodeAnalysis();

    // NOTE: Can only choose one of these.
//    witnessCodeAnalysis.analyseUpTo(3);
//    witnessCodeAnalysis.dumpOne(271984);
    witnessCodeAnalysis.analyseOne(266634);

//    witnessCodeAnalysis.analyseDeployedBlockNumbers(9999990, 10000000);

//    witnessCodeAnalysis.analyseAll();

    witnessCodeAnalysis.showSummary();
  }
}
