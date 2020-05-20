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

import tech.pegasys.poc.witnesscodeanalysis.vm.operations.BeginSubOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;

import java.util.BitSet;

import com.google.common.base.MoreObjects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;

/** Represents EVM code associated with an account. */
public class Code implements Cloneable {

  /** The bytes representing the code. */
  private Bytes bytes;

  /** Used to cache valid jump destinations. */
  private BitSet validJumpDestinations;

  /** Used to cache valid jump sub destinations. */
  private BitSet validJumpSubDestinations;

  /**
   * Public constructor.
   *
   * @param bytes The byte representation of the code.
   */
  public Code(final Bytes bytes) {
    this.bytes = bytes;
  }

  public Code() {
    this(Bytes.EMPTY);
  }

  /**
   * Returns true if the object is equal to this; otherwise false.
   *
   * @param other The object to compare this with.
   * @return True if the object is equal to this; otherwise false.
   */
  @Override
  public boolean equals(final Object other) {
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof Code)) return false;

    final Code that = (Code) other;
    return this.bytes.equals(that.bytes);
  }

  @Override
  public int hashCode() {
    return bytes.hashCode();
  }

  /** @return The number of bytes in the code. */
  public int getSize() {
    return bytes.size();
  }

  public Bytes getBytes() {
    return bytes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("bytes", bytes).toString();
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    Code cloneObj = (Code) super.clone();
    cloneObj.bytes = this.bytes.copy();
    if (this.validJumpDestinations != null) {
      cloneObj.validJumpDestinations = (BitSet) this.validJumpDestinations.clone();
    }
    if (this.validJumpSubDestinations != null) {
      cloneObj.validJumpSubDestinations = (BitSet) this.validJumpSubDestinations.clone();
    }
    return cloneObj;
  }
}
