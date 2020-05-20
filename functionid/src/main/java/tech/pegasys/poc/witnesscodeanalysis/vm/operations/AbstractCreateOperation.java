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

import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.MessageFrame;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

public abstract class AbstractCreateOperation extends AbstractOperation {

  public AbstractCreateOperation(
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

  @Override
  public void execute(final MessageFrame frame) {
    throw new Error("Not implemented yet");
//    final Wei value = Wei.wrap(frame.getStackItem(0));
//
//    final Address address = frame.getRecipientAddress();
//    final MutableAccount account = frame.getWorldState().getAccount(address).getMutable();
//
//    frame.clearReturnData();
//
//    if (value.compareTo(account.getBalance()) > 0 || frame.getMessageStackDepth() >= 1024) {
//      fail(frame);
//    } else {
//      spawnChildMessage(frame);
//    }
  }

  private void fail(final MessageFrame frame) {
    final UInt256 inputOffset = UInt256.fromBytes(frame.getStackItem(1));
    final UInt256 inputSize = UInt256.fromBytes(frame.getStackItem(2));
//    frame.readMemory(inputOffset, inputSize);
    frame.popStackItems(getStackItemsConsumed());
    frame.pushStackItem(Bytes32.ZERO);
  }

//  private void spawnChildMessage(final MessageFrame frame) {
//    final Address address = frame.getRecipientAddress();
//    final MutableAccount account = frame.getWorldState().getAccount(address).getMutable();
//
//    account.incrementNonce();
//
//    final Wei value = Wei.wrap(frame.getStackItem(0));
//    final UInt256 inputOffset = UInt256.fromBytes(frame.getStackItem(1));
//    final UInt256 inputSize = UInt256.fromBytes(frame.getStackItem(2));
//    final Bytes inputData = frame.readMemory(inputOffset, inputSize);
//
//    final Address contractAddress = targetContractAddress(frame);
//
//    final Gas childGasStipend = gasCalculator().gasAvailableForChildCreate(frame.getRemainingGas());
//    frame.decrementRemainingGas(childGasStipend);
//
//    final MessageFrame childFrame =
//        MessageFrame.builder()
//            .type(MessageFrame.Type.CONTRACT_CREATION)
//            .messageFrameStack(frame.getMessageFrameStack())
//            .blockchain(frame.getBlockchain())
//            .worldState(frame.getWorldState().updater())
//            .initialGas(childGasStipend)
//            .address(contractAddress)
//            .originator(frame.getOriginatorAddress())
//            .contract(contractAddress)
//            .contractAccountVersion(frame.getContractAccountVersion())
//            .gasPrice(frame.getGasPrice())
//            .inputData(Bytes.EMPTY)
//            .sender(frame.getRecipientAddress())
//            .value(value)
//            .apparentValue(value)
//            .code(new Code(inputData))
//            .blockHeader(frame.getBlockHeader())
//            .depth(frame.getMessageStackDepth() + 1)
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
//  private void complete(final MessageFrame frame, final MessageFrame childFrame) {
//    frame.setState(MessageFrame.State.CODE_EXECUTING);
//
//    frame.incrementRemainingGas(childFrame.getRemainingGas());
//    frame.addLogs(childFrame.getLogs());
//    frame.addSelfDestructs(childFrame.getSelfDestructs());
//    frame.incrementGasRefund(childFrame.getGasRefund());
//
//    frame.popStackItems(getStackItemsConsumed());
//
//    if (childFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
//      frame.pushStackItem(Words.fromAddress(childFrame.getContractAddress()));
//    } else {
//      frame.setReturnData(childFrame.getOutputData());
//      frame.pushStackItem(Bytes32.ZERO);
//    }
//
//    final int currentPC = frame.getPC();
//    frame.setPC(currentPC + 1);
//  }
}
