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
package tech.pegasys.poc.witnesscodeanalysis.vm.operations;


import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;

import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

public class ReturnDataCopyOperation extends AbstractOperation {
  public static final int OPCODE = 0x3E;

  public ReturnDataCopyOperation() {
    super(OPCODE, "RETURNDATACOPY", 3, 0, 1);
  }

  @Override
  public UInt256 execute(final MessageFrame frame) {
    frame.popStackItem();
    frame.popStackItem();
    frame.popStackItem();
    return UInt256.ZERO;
  }
}
