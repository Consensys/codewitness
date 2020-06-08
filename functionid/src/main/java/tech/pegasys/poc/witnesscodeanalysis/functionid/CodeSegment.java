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
package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.logging.log4j.Logger;
import tech.pegasys.poc.witnesscodeanalysis.common.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperandStack;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CodeSegment {
  private static final Logger LOG = getLogger();
  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public static final int INVALID = -1;
  public int start;
  public int length = INVALID;
  public boolean endsProgram = false;
  public boolean happyPathEnding = false;
  public ArrayList<Integer> previousSegments = new ArrayList<>();
  public ArrayList<OperandStack> previousSegmentStacks = new ArrayList<>();
  public int nextSegmentNoJump = INVALID;
  public Set<Integer> nextSegmentJumps = new HashSet<>();
  private int lastOpCode = INVALID;

  public CodeSegment(int start, int callingSegmentPc, OperandStack callingSegmentStack) {
    this.start = start;
    this.previousSegments.add(callingSegmentPc);
    this.previousSegmentStacks.add(callingSegmentStack);
  }

  public void addNewPrevious(int callingSegmentPc, OperandStack callingSegmentStack) {
    this.previousSegments.add(callingSegmentPc);
    this.previousSegmentStacks.add(callingSegmentStack);

    if (this.previousSegments.size() > 50) {
      LOG.info("{} previous segments for PC {}", this.previousSegments.size(), PcUtils.pcStr(start));
      throw new Error("Not detecting previous correctly");
    }
  }

  // Jump or fall through, does not end.
  public void setValuesJumpi(int len, int nextSegmentJump, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    addJumpIfNotSetAlready(nextSegmentJump);
    setNoJump();
  }

  // Fall through only, does not end.
  public void setValuesJumpDest(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    setNoJump();
  }

  // Jump only, does not end.
  public void setValuesJump(int len, int nextSegmentJump, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    addJumpIfNotSetAlready(nextSegmentJump);
  }

  // Return, does not end.
  public void setValuesReturnSub(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
  }

  public void setValuesHappyEnding(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    this.endsProgram = true;
    this.happyPathEnding = true;
  }

  public void setValuesSadEnding(int len, int lastOpCode) {
    setLenLastOpCode(len, lastOpCode);
    this.endsProgram = true;
  }

  private void setLenLastOpCode(int len, int lastOpCode) {
    if (this.length != INVALID) {
      if (this.length != len && this.lastOpCode != lastOpCode) {
        throw new Error("Setting ReturnSub a second time with inconsistent values");
      }
      return;
    }
    this.length = len;
    this.lastOpCode = lastOpCode;
  }

  private void addJumpIfNotSetAlready(int nextSegmentJump) {
    this.nextSegmentJumps.add(nextSegmentJump);
  }

  private void setNoJump() {
    // If the start and the length are known, then the next segment no jump will always be the same.
    this.nextSegmentNoJump = this.start + this.length;
  }


  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Code Segment: previous: ");
    if (this.previousSegments.isEmpty()) {
      buf.append("none, ");
    }
    else {
      for (int previousSegment: this.previousSegments) {
        buf.append(previousSegment);
        buf.append(", ");
      }
    }
    buf.append("start: ");
    buf.append(this.start);
    buf.append(", length: ");
    buf.append(this.length);
    buf.append(", next: ");
    buf.append(this.nextSegmentNoJump);
    buf.append(", jumpdest: ");
    if (this.nextSegmentJumps.isEmpty()) {
      buf.append("none, ");
    } else {
      for (int nextSegmentJump: this.nextSegmentJumps) {
        buf.append(nextSegmentJump);
        buf.append(", ");
      }
    }
    buf.append(", End of program: ");
    buf.append(this.endsProgram);
    if (this.endsProgram) {
      buf.append(", Happy Path Ending: ");
      buf.append(this.happyPathEnding);
    }
    if (this.lastOpCode != INVALID) {
      buf.append(", Last OpCode: ");
      buf.append(registry.get(this.lastOpCode, 0).getName());
    }
    return buf.toString();
  }
}
