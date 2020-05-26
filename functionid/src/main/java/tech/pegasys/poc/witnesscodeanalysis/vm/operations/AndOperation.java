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
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

import org.apache.tuweni.units.bigints.UInt256;

public class AndOperation extends AbstractOperation {
  public static int OPCODE = 0x16;
  public static Bytes32 MARKER_AND_OPCODE = UInt256.valueOf(DYNAMIC_MARKER + OPCODE).toBytes();

  public AndOperation() {
    super(OPCODE, "AND", 2, 1, 1);
  }

  @Override
  public UInt256 execute(final MessageFrame frame) {
    final UInt256 value0 = UInt256.fromBytes(frame.popStackItem());
    final UInt256 value1 = UInt256.fromBytes(frame.popStackItem());

    final UInt256 result = value0.and(value1);

    frame.pushStackItem(result.toBytes());
//
//
//    frame.popStackItem();
//    frame.popStackItem();
//    frame.pushStackItem(MARKER_AND_OPCODE);

    return UInt256.ZERO;
  }
}
