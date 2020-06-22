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

import static java.lang.Math.min;


import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;

import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes32;

public class PushOperation extends AbstractOperation {
  public static final int PUSH1_OPCODE = 0x60;
  public static final int PUSH2_OPCODE = 0x61;
  public static final int PUSH3_OPCODE = 0x62;
  public static final int PUSH4_OPCODE = 0x63;

  private final int length;

  public PushOperation(final int length) {
    super(0x60 + length - 1, "PUSH" + length, 0, 1,length + 1);
    this.length = length;
  }

  @Override
  public UInt256 execute(final MessageFrame frame) {
    final int pc = frame.getPC();
    final Bytes code = frame.getCode().getBytes();

    final int copyLength = min(length, code.size() - pc - 1);
    final MutableBytes32 bytes = MutableBytes32.create();
    code.slice(pc + 1, copyLength).copyTo(bytes, bytes.size() - length);
    frame.pushStackItem(bytes);
    return UInt256.ZERO;
  }
}
