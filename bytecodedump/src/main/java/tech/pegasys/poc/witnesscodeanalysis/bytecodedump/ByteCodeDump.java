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
package tech.pegasys.poc.witnesscodeanalysis.bytecodedump;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.CodeAnalysisBase;


/**
 * Prints out the program counter offset, opcodes and parameter. For example:
 *
 * PC: 0x5d5d, opcode: PUSH1 0x20
 * PC: 0x5d5f, opcode: ADD
 * PC: 0x5d60, opcode: PUSH1 0x00
 * PC: 0x5d62, opcode: DUP2
 * PC: 0x5d63, opcode: MSTORE
 * PC: 0x5d64, opcode: PUSH1 0x20
 * PC: 0x5d66, opcode: ADD
 * PC: 0x5d67, opcode: PUSH1 0x00
 */
public class ByteCodeDump extends CodeAnalysisBase {
  public ByteCodeDump(Bytes code) {
    super(code);
  }

  public void dumpContract() {
    ByteCodePrinter printer = new ByteCodePrinter(this.code);
    printer.print(0, this.simple.getEndOfCode() + 1);
  }

  public static final String c2 = "6080604052348015600f57600080fd5b506004361060285760003560e01c8063ed3f50fb14602d575b600080fd5b609760048036036040811015604157600080fd5b810190602081018135640100000000811115605b57600080fd5b820183602082011115606c57600080fd5b80359060200191846001830284011164010000000083111715608d57600080fd5b9193509150356099565b005b8060008484604051808383808284379190910194855250506040519283900360200190922092909255505050505056fea265627a7a723058204ee502f76959c5eab846114b0f307607dfd14920b9f726bd84894b10d37745a264736f6c634300050a0032";


  public static void main(String[] args) {
//    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0x63de3096c22e89f175c8ed51ca0c129118516979);
//    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0x6475593a8c52aac4059b1eb68235004f136eda5d);
    Bytes code = Bytes.fromHexString(c2);

    ByteCodeDump dump = new ByteCodeDump(code);
    //dump.showBasicInfo();
    dump.dumpContract();
  }
}
