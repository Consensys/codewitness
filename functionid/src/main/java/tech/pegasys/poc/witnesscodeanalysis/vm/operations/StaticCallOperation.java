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
import tech.pegasys.poc.witnesscodeanalysis.vm.Address;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractCallOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;
import tech.pegasys.poc.witnesscodeanalysis.vm.Words;

import org.apache.tuweni.units.bigints.UInt256;

public class StaticCallOperation extends AbstractCallOperation {
  public static final int OPCODE = 0xFA;

  public StaticCallOperation() {
    super(OPCODE, "STATICCALL", 6, 1, 1);
  }

  @Override
  protected Address to(final MessageFrame frame) {
    return Words.toAddress(frame.getStackItem(1));
  }

  @Override
  protected UInt256 inputDataOffset(final MessageFrame frame) {
    return UInt256.fromBytes(frame.getStackItem(2));
  }

  @Override
  protected UInt256 inputDataLength(final MessageFrame frame) {
    return UInt256.fromBytes(frame.getStackItem(3));
  }

  @Override
  protected UInt256 outputDataOffset(final MessageFrame frame) {
    return UInt256.fromBytes(frame.getStackItem(4));
  }

  @Override
  protected UInt256 outputDataLength(final MessageFrame frame) {
    return UInt256.fromBytes(frame.getStackItem(5));
  }

  @Override
  protected boolean isStatic(final MessageFrame frame) {
    return true;
  }
}
