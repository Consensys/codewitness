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


import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcess;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.Code;

import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

import org.apache.tuweni.units.bigints.UInt256;

import java.util.ArrayList;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodeCopyOperation extends AbstractOperation {
  private static final Logger LOG = getLogger();

  public static int OPCODE = 0x39;

  private static BasicBlockConsumer consumer = null;

  public CodeCopyOperation() {
    super(OPCODE, "CODECOPY", 3, 0, 1);
  }

  @Override
  public UInt256 execute(final MessageFrame frame) {
    final Code code = frame.getCode();

    final UInt256 memOffset = UInt256.fromBytes(frame.popStackItem());
    final UInt256 sourceOffset = UInt256.fromBytes(frame.popStackItem());
    final UInt256 numBytes = UInt256.fromBytes(frame.popStackItem());

    int start = sourceOffset.intValue();
    int len = numBytes.intValue();


    // If either of the inputs are dynamic then the output is dynamic.
    boolean isConstantInput = true;
    if ((start & DYNAMIC_MARKER_MASK) == DYNAMIC_MARKER) {
      isConstantInput = false;
    }
    if ((len & DYNAMIC_MARKER_MASK) == DYNAMIC_MARKER) {
      isConstantInput = false;
    }
    if (isConstantInput) {
      Bytes codeFragment = code.getBytes().slice(start, len);
      BasicBlockWithCode block = new BasicBlockWithCode(start, len, codeFragment);

      if (consumer != null) {
        consumer.addNewBlock(block);
      }
    }
    else {
      String message = "Start: 0x" + Integer.toHexString(start) + " Length: 0x" + Integer.toHexString(len);
      UnableToProcess.getInstance().unableToProcess(UnableToProcessReason.CODECOPY_WITH_DYNAMIC_PARAMETERS, message);
    }

    return UInt256.ZERO;
  }

  public interface BasicBlockConsumer {
    void addNewBlock(BasicBlockWithCode block);
  }

  public static void setConsumer(BasicBlockConsumer consumerImpl) {
    consumer = consumerImpl;
  }

  public static void removeConsumer() {
    consumer = null;
  }

}
