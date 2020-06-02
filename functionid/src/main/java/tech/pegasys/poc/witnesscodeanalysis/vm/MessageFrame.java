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

import tech.pegasys.poc.witnesscodeanalysis.vm.operations.ReturnStack;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.tuweni.bytes.Bytes32;

/**
 * A container object for all of the state associated with a message.
 *
 * <p>A message corresponds to an interaction between two accounts. A Transaction spawns at
 * least one message when its processed. Messages can also spawn messages depending on the code
 * executed within a message.
 *
 * <p>Note that there is no specific Message object in the code base. Instead message executions
 * correspond to a {@code MessageFrame} and a specific AbstractMessageProcessor. Currently
 * there are two such AbstractMessageProcessor types:
 *
 * <p><b>Message Call ({@code MESSAGE_CALL})</b>
 *
 * <p>A message call consists of applying a set of changes to an account on behalf of another
 * account. At the minimal end of changes is a value transfer between a sender and recipient
 * account. If the recipient account contains code, that code is also executed.
 *
 * <p><b>Contract Creation ({@code CONTRACT_CREATION})</b>
 *
 * <p>A contract creation, as its name suggests, creates contract accounts. Contract initialization
 * code and a value are supplied to initialize the contract account code and balance, respectively.
 */
public class MessageFrame implements Cloneable {

  /**
   * Message Frame State.
   *
   * <h2>Message Frame Lifecycle</h2>
   *
   * <p>The diagram below presents the message frame lifecycle:
   *
   * <pre>
   *            ------------------------------------------------------
   *            |                                                    |
   *            |                                                    v
   *            |               ---------------------     ---------------------
   *            |               |                   |     |                   |
   *            |               |    CODE_SUCCESS   | --&gt; | COMPLETED_SUCCESS |
   *            |               |                   |     |                   |
   *            |               ---------------------     ---------------------
   *            |                         ^
   *            |                         |
   *  ---------------------     ---------------------     ---------------------
   *  |                   |     |                   | --&gt; |                   |
   *  |    NOT_STARTED    | --&gt; |   CODE_EXECUTING  |     |   CODE_SUSPENDED  |
   *  |                   |     |                   | &lt;-- |                   |
   *  ---------------------     ---------------------     ---------------------
   *            |                         |
   *            |                         |
   *            |                         |                 ---------------------
   *            |                         |                 |                   |
   *            |                         |------------&gt; |      REVERTED     |
   *            |                         |                 |                   |
   *            |                         |                 ---------------------
   *            |                         |
   *            |                         v
   *            |               ---------------------     ---------------------
   *            |               |                   |     |                   |
   *            |-------------&gt; |  EXCEPTIONAL_HALT | --&gt; | COMPLETED_FAILURE |
   *                            |                   |     |                   |
   *                            ---------------------     ---------------------
   * </pre>
   *
   * <h3>Message Not Started ({@link #NOT_STARTED})</h3>
   *
   * <p>The message has not begun to execute yet.
   *
   * <h3>Code Executing ({@link #CODE_EXECUTING})</h3>
   *
   * <p>The message contains code and has begun executing it. The execution will continue until it
   * is halted due to (1) spawning a child message (2) encountering an exceptional halting condition
   * (2) completing successfully.
   *
   * <h3>Code Suspended Execution ({@link #CODE_SUSPENDED})</h3>
   *
   * <p>The message has spawned a child message and has suspended its execution until the child
   * message has completed and notified its parent message. The message will then continue executing
   * code ({@link #CODE_EXECUTING}) again.
   *
   * <h3>Code Execution Completed Successfully ({@link #CODE_SUSPENDED})</h3>
   *
   * <p>The code within the message has executed to completion successfully.
   *
   * <h3>Message Exceptionally Halted ({@link #EXCEPTIONAL_HALT})</h3>
   *
   * <p>The message execution has encountered an exceptional halting condition at some point during
   * its execution.
   *
   * <h3>Message Reverted ({@link #REVERT})</h3>
   *
   * <p>The message execution has requested to revert state during execution.
   *
   * <h3>Message Execution Failed ({@link #COMPLETED_FAILED})</h3>
   *
   * <p>The message execution failed to execute successfully; most likely due to encountering an
   * exceptional halting condition. At this point the message frame is finalized and the parent is
   * notified.
   *
   * <h3>Message Execution Completed Successfully ({@link #COMPLETED_SUCCESS})</h3>
   *
   * <p>The message execution completed successfully and needs to finalized and propagated to the
   * parent message that spawned it.
   */
  public enum State {

    /** Message execution has not started. */
    NOT_STARTED,

    /** Code execution within the message is in progress. */
    CODE_EXECUTING,

    /** Code execution within the message has finished successfully. */
    CODE_SUCCESS,

    /** Code execution within the message has been suspended. */
    CODE_SUSPENDED,

    /** An exceptional halting condition has occurred. */
    EXCEPTIONAL_HALT,

    /** State changes were reverted during execution. */
    REVERT,

    /** The message execution has failed to complete successfully. */
    COMPLETED_FAILED,

    /** The message execution has completed successfully. */
    COMPLETED_SUCCESS,
  }



  public static final int DEFAULT_MAX_STACK_SIZE = 1024;

  // as defined on https://eips.ethereum.org/EIPS/eip-2315
  public static final int DEFAULT_MAX_RETURN_STACK_SIZE = 1023;


  // Metadata fields.
  private State state;

  // Machine state fields.
  private final int maxStackSize;
  private int pc;
  private OperandStack stack;
  private final boolean isStatic;

  // Execution Environment fields.
  private Code code;
  private final int depth;
  private Deque<MessageFrame> messageFrameStack;

  // as defined on https://eips.ethereum.org/EIPS/eip-2315
  private ReturnStack returnStack;


  private Operation currentOperation;

  public static Builder builder() {
    return new Builder();
  }

  private MessageFrame(
      final Deque<MessageFrame> messageFrameStack,
      final ReturnStack returnStack,
      final Code code,
      final int depth,
      final boolean isStatic,
      final int maxStackSize) {
    this.messageFrameStack = messageFrameStack;
    this.returnStack = returnStack;
    this.maxStackSize = maxStackSize;
    this.pc = 0;
    this.stack = new PreAllocatedOperandStack(maxStackSize);
    this.code = code;
    this.depth = depth;
    this.state = State.NOT_STARTED;
    this.isStatic = isStatic;
  }


  /**
   * Return the program counter.
   *
   * @return the program counter
   */
  public int getPC() {
    return pc;
  }

  /**
   * Set the program counter.
   *
   * @param pc The new program counter value
   */
  public void setPC(final int pc) {
    this.pc = pc;
  }

  /**
   * Returns the item at the specified offset in the stack.
   *
   * @param offset The item's position relative to the top of the stack
   * @return The item at the specified offset in the stack
   * @throws IndexOutOfBoundsException if the offset is out of range
   */
  public Bytes32 getStackItem(final int offset) {
    return stack.get(offset);
  }

  /**
   * Removes the item at the top of the stack.
   *
   * @return the item at the top of the stack
   * @throws IllegalStateException if the stack is empty
   */
  public Bytes32 popStackItem() {
    return stack.pop();
  }

  public OperandStack getCopyOfStack() {
    return (OperandStack) ((PreAllocatedOperandStack) this.stack).clone();
  }

  /**
   * Removes the corresponding number of items from the top of the stack.
   *
   * @param n The number of items to pop off the stack
   * @throws IllegalStateException if the stack does not contain enough items
   */
  public void popStackItems(final int n) {
    stack.bulkPop(n);
  }

  /**
   * Pushes the corresponding item onto the top of the stack
   *
   * @param value The value to push onto the stack.
   * @throws IllegalStateException if the stack is full
   */
  public void pushStackItem(final Bytes32 value) {
    stack.push(value);
  }

  /**
   * Sets the stack item at the specified offset from the top of the stack to the value
   *
   * @param offset The item's position relative to the top of the stack
   * @param value The value to set the stack item to
   * @throws IllegalStateException if the stack is too small
   */
  public void setStackItem(final int offset, final Bytes32 value) {
    stack.set(offset, value);
  }

  /**
   * Return the current stack size.
   *
   * @return The current stack size
   */
  public int stackSize() {
    return stack.size();
  }

  /**
   * Tests if the return stack is full
   *
   * @return true is the return stack is full, else false
   */
  public boolean isReturnStackFull() {
    return returnStack.isFull();
  }

  /**
   * Tests if the return stack is empty
   *
   * @return true is the return stack is empty, else false
   */
  public boolean isReturnStackEmpty() {
    return returnStack.isEmpty();
  }

  /**
   * Removes the item at the top of the return stack.
   *
   * @return the item at the top of the return stack
   * @throws IllegalStateException if the return stack is empty
   */
  public int popReturnStackItem() {
    return returnStack.pop();
  }

  /**
   * Return the return stack.
   *
   * @return the return stack
   */
  public ReturnStack getReturnStack() {
    return returnStack;
  }

  /**
   * Pushes the corresponding item onto the top of the return stack
   *
   * @param value The value to push onto the return stack.
   * @throws IllegalStateException if the stack is full
   */
  public void pushReturnStackItem(final int value) {
    returnStack.push(value);
  }

  /**
   * Returns whether or not the message frame is static or not.
   *
   * @return {@code} true if the frame is static; otherwise {@code false}
   */
  public boolean isStatic() {
    return isStatic;
  }


  /**
   * Returns the current execution state.
   *
   * @return the current execution state
   */
  public State getState() {
    return state;
  }

  /**
   * Sets the current execution state.
   *
   * @param state The new execution state
   */
  public void setState(final State state) {
    this.state = state;
  }

  /**
   * Returns the code currently being executed.
   *
   * @return the code currently being executed
   */
  public Code getCode() {
    return code;
  }



  /**
   * Returns the message stack depth.
   *
   * @return the message stack depth
   */
  public int getMessageStackDepth() {
    return depth;
  }


  /**
   * Returns the current message frame stack.
   *
   * @return the current message frame stack
   */
  public Deque<MessageFrame> getMessageFrameStack() {
    return messageFrameStack;
  }

  public Operation getCurrentOperation() {
    return currentOperation;
  }

  public int getMaxStackSize() {
    return maxStackSize;
  }


  public void setCurrentOperation(final Operation currentOperation) {
    this.currentOperation = currentOperation;
  }


  public Object clone() {
    MessageFrame cloneObj = null;
    try {
      cloneObj = (MessageFrame) super.clone();
      cloneObj.stack = (PreAllocatedOperandStack) ((PreAllocatedOperandStack) stack).clone();
      cloneObj.code = (Code) code.clone();
      cloneObj.messageFrameStack = ((ArrayDeque<MessageFrame>)this.messageFrameStack).clone();
      cloneObj.returnStack = (ReturnStack) this.returnStack.clone();
      cloneObj.currentOperation = (Operation) ((AbstractOperation) currentOperation).clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new Error("Unexpectedly clone not supported");
    }
    return cloneObj;
  }


  public static class Builder {
    private Deque<MessageFrame> messageFrameStack;
    private Code code;
    private int depth = -1;
    private int maxStackSize = DEFAULT_MAX_STACK_SIZE;
    private boolean isStatic = false;
    private ReturnStack returnStack = new ReturnStack(MessageFrame.DEFAULT_MAX_RETURN_STACK_SIZE);

    public Builder returnStack(final ReturnStack returnStack) {
      this.returnStack = returnStack;
      return this;
    }

    public Builder messageFrameStack(final Deque<MessageFrame> messageFrameStack) {
      this.messageFrameStack = messageFrameStack;
      return this;
    }

    public Builder code(final Code code) {
      this.code = code;
      return this;
    }

    public Builder depth(final int depth) {
      this.depth = depth;
      return this;
    }

    public Builder isStatic(final boolean isStatic) {
      this.isStatic = isStatic;
      return this;
    }

    public Builder maxStackSize(final int maxStackSize) {
      this.maxStackSize = maxStackSize;
      return this;
    }

    public MessageFrame build() {
      return new MessageFrame(
          messageFrameStack,
          returnStack,
          code,
          depth,
          isStatic,
          maxStackSize);
    }
  }
}
