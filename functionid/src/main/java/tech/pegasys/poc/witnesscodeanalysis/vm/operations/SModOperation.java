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


import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;

import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class SModOperation extends AbstractOperation {

  public SModOperation() {
    super(0x07, "SMOD", 2, 1, 1);
  }

  @Override
  public UInt256 execute(final MessageFrame frame) {
    final Bytes32 value0 = frame.popStackItem();
    final Bytes32 value1 = frame.popStackItem();

    if (value1.isZero()) {
      frame.pushStackItem(Bytes32.ZERO);
    } else {
      BigInteger b1 = value0.toBigInteger();
      BigInteger b2 = value1.toBigInteger();
      BigInteger result = b1.abs().mod(b2.abs());
      if (b1.signum() < 0) {
        result = result.negate();
      }

      Bytes resultBytes = Bytes.wrap(result.toByteArray());
      if (resultBytes.size() > 32) {
        resultBytes = resultBytes.slice(resultBytes.size() - 32, 32);
      }

      byte[] padding = new byte[32 - resultBytes.size()];
      Arrays.fill(padding, result.signum() < 0 ? (byte) 0xFF : 0x00);

      frame.pushStackItem(Bytes32.wrap(Bytes.concatenate(Bytes.wrap(padding), resultBytes)));
    }
    return UInt256.ZERO;
  }
}
