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
package tech.pegasys.poc.witnesscodeanalysis.vm;

import org.apache.tuweni.units.bigints.UInt256;

/**
 * A skeleton class for implementing call operations.
 *
 * <p>A call operation creates a child message call from the current message context, allows it to
 * execute, and then updates the current message context based on its execution.
 */
public abstract class AbstractCallOperation extends AbstractOperation {

  public AbstractCallOperation(
      final int opcode,
      final String name,
      final int stackItemsConsumed,
      final int stackItemsProduced,
      final int opSize) {
    super(
        opcode,
        name,
        stackItemsConsumed,
        stackItemsProduced,
        opSize);
  }

  /**
   * Returns the account the call is being made to.
   *
   * @param frame The current message frame
   * @return the account the call is being made to
   */
  protected abstract Address to(MessageFrame frame);

  /**
   * Returns the memory offset the input data starts at.
   *
   * @param frame The current message frame
   * @return the memory offset the input data starts at
   */
  protected abstract UInt256 inputDataOffset(MessageFrame frame);

  /**
   * Returns the length of the input data to read from memory.
   *
   * @param frame The current message frame
   * @return the length of the input data to read from memory.
   */
  protected abstract UInt256 inputDataLength(MessageFrame frame);

  /**
   * Returns the memory offset the offset data starts at.
   *
   * @param frame The current message frame
   * @return the memory offset the offset data starts at
   */
  protected abstract UInt256 outputDataOffset(MessageFrame frame);

  /**
   * Returns the length of the output data to read from memory.
   *
   * @param frame The current message frame
   * @return the length of the output data to read from memory.
   */
  protected abstract UInt256 outputDataLength(MessageFrame frame);

  /**
   * Returns whether or not the child message call should be static.
   *
   * @param frame The current message frame
   * @return {@code true} if the child message call should be static; otherwise {@code false}
   */
  protected abstract boolean isStatic(MessageFrame frame);

  @Override
  public UInt256 execute(final MessageFrame frame) {
    throw new Error("TODO Not implemented yet");

//
//    frame.clearReturnData();
//
//    final Address to = to(frame);
//    final Account contract = frame.getWorldState().get(to);
//
//    final Account account = frame.getWorldState().get(frame.getRecipientAddress());
//    final Wei balance = account.getBalance();
//    if (value(frame).compareTo(balance) > 0 || frame.getMessageStackDepth() >= 1024) {
//      frame.expandMemory(inputDataOffset(frame).toLong(), inputDataLength(frame).intValue());
//      frame.expandMemory(outputDataOffset(frame).toLong(), outputDataLength(frame).intValue());
//      frame.incrementRemainingGas(gasAvailableForChildCall(frame));
//      frame.popStackItems(getStackItemsConsumed());
//      frame.pushStackItem(Bytes32.ZERO);
//      return;
//    }
//
//    final Bytes inputData = frame.readMemory(inputDataOffset(frame), inputDataLength(frame));
//
//    final MessageFrame childFrame =
//        MessageFrame.builder()
//            .type(MessageFrame.Type.MESSAGE_CALL)
//            .messageFrameStack(frame.getMessageFrameStack())
//            .blockchain(frame.getBlockchain())
//            .worldState(frame.getWorldState().updater())
//            .initialGas(gasAvailableForChildCall(frame))
//            .address(address(frame))
//            .originator(frame.getOriginatorAddress())
//            .contract(to)
//            .contractAccountVersion(
//                contract != null ? contract.getVersion() : Account.DEFAULT_VERSION)
//            .gasPrice(frame.getGasPrice())
//            .inputData(inputData)
//            .sender(sender(frame))
//            .value(value(frame))
//            .apparentValue(apparentValue(frame))
//            .code(new Code(contract != null ? contract.getCode() : Bytes.EMPTY))
//            .blockHeader(frame.getBlockHeader())
//            .depth(frame.getMessageStackDepth() + 1)
//            .isStatic(isStatic(frame))
//            .completer(child -> complete(frame, child))
//            .miningBeneficiary(frame.getMiningBeneficiary())
//            .blockHashLookup(frame.getBlockHashLookup())
//            .maxStackSize(frame.getMaxStackSize())
//            .returnStack(frame.getReturnStack())
//            .build();
//
//    frame.getMessageFrameStack().addFirst(childFrame);
//    frame.setState(MessageFrame.State.CODE_SUSPENDED);
//  }
//
//  public void complete(final MessageFrame frame, final MessageFrame childFrame) {
//    frame.setState(MessageFrame.State.CODE_EXECUTING);
//
//    final UInt256 outputOffset = outputDataOffset(frame);
//    final UInt256 outputSize = outputDataLength(frame);
//    final Bytes outputData = childFrame.getOutputData();
//    final int outputSizeAsInt = outputSize.intValue();
//
//    if (outputSizeAsInt > outputData.size()) {
//      frame.expandMemory(outputOffset.toLong(), outputSizeAsInt);
//      frame.writeMemory(outputOffset, UInt256.valueOf(outputData.size()), outputData, true);
//    } else {
//      frame.writeMemory(outputOffset, outputSize, outputData, true);
//    }
//
//    frame.setReturnData(outputData);
//    frame.addLogs(childFrame.getLogs());
//    frame.addSelfDestructs(childFrame.getSelfDestructs());
//    frame.incrementGasRefund(childFrame.getGasRefund());
//
//    final Gas gasRemaining = childFrame.getRemainingGas();
//    frame.incrementRemainingGas(gasRemaining);
//
//    frame.popStackItems(getStackItemsConsumed());
//
//    if (childFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
//      frame.pushStackItem(UInt256.ONE.toBytes());
//    } else {
//      frame.pushStackItem(Bytes32.ZERO);
//    }
//
//    final int currentPC = frame.getPC();
//    frame.setPC(currentPC + 1);
  }
}
