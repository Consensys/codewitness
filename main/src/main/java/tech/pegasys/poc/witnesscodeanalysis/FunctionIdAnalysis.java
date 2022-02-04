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

public class FunctionIdAnalysis {
  private static final Logger LOG = getLogger();

  public static final String erc20 = "608060405234801561001057600080fd5b506004361061002b5760003560e01c8063b27b880414610030575b600080fd5b61004a60048036038101906100459190610156565b61004c565b005b60606000806000604051935036600085376000803686885af490503d9150816000853e806000811461007d57610093565b60008311156100925761012085019350836040525b5b5060008114156100ec578473ffffffffffffffffffffffffffffffffffffffff167f410d96db3f80b0f89b36888c4d8a94004268f8d42309ac39b7bcba706293e099856040516100e3919061021c565b60405180910390a25b5050505050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610123826100f8565b9050919050565b61013381610118565b811461013e57600080fd5b50565b6000813590506101508161012a565b92915050565b60006020828403121561016c5761016b6100f3565b5b600061017a84828501610141565b91505092915050565b600081519050919050565b600082825260208201905092915050565b60005b838110156101bd5780820151818401526020810190506101a2565b838111156101cc576000848401525b50505050565b6000601f19601f8301169050919050565b60006101ee82610183565b6101f8818561018e565b935061020881856020860161019f565b610211816101d2565b840191505092915050565b6000602082019050818103600083015261023681846101e3565b90509291505056fea2646970667358221220a912bed394bc886030fbe9972ff5be5f4ed7fa944598b8766ec8a711ad71c7f764736f6c63430008090033";

  public static boolean SIMPLE = true;
  public static boolean FUNCTIONID = true;

  private SimpleProcessing simpleProcessing;
  private FunctionIdProcessing functionIdProcessing;

  Bytes code;

  public FunctionIdAnalysis(Bytes code) throws IOException {
    this.code = code;
    this.simpleProcessing = new SimpleProcessing(true);
    this.functionIdProcessing = new FunctionIdProcessing(true);
  }





  public void analyse() throws Exception {
    ContractData contractData = new ContractData();
    contractData.setCode(this.code.toHexString());
    contractData.setContract_address(new String[]{"NONE"});
    process(1, contractData);
    closeAll();
  }


  public void dump() throws Exception {
    ByteCodeDump dump = new ByteCodeDump(this.code);
    dump.showBasicInfo();
    dump.dumpContract();
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
    this.simpleProcessing.close();
    this.functionIdProcessing.close();
  }




  public static void main(String[] args) throws Exception {
    Bytes code = Bytes.fromHexString(erc20);

    FunctionIdAnalysis analysis = new FunctionIdAnalysis(code);

//    analysis.dump();
    analysis.analyse();
    analysis.showSummary();
  }

}
