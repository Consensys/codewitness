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

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.DelegatingBytes;

/** A 160-bits account address. */
public class Address extends DelegatingBytes {

  public static final int SIZE = 20;

  /** Specific addresses of the "precompiled" contracts. */
  public static final Address ECREC = Address.precompiled(1);

  public static final Address SHA256 = Address.precompiled(2);
  public static final Address RIPEMD160 = Address.precompiled(3);
  public static final Address ID = Address.precompiled(4);
  public static final Address MODEXP = Address.precompiled(5);
  public static final Address ALTBN128_ADD = Address.precompiled(6);
  public static final Address ALTBN128_MUL = Address.precompiled(7);
  public static final Address ALTBN128_PAIRING = Address.precompiled(8);
  public static final Address BLAKE2B_F_COMPRESSION = Address.precompiled(9);

  // Last address that can be generated for a pre-compiled contract
  public static final Integer PRIVACY = Byte.MAX_VALUE - 1;
  public static final Address DEFAULT_PRIVACY = Address.precompiled(PRIVACY);
  public static final Address ONCHAIN_PRIVACY = Address.precompiled(PRIVACY - 1);
  public static final Address PRIVACY_PROXY = Address.precompiled(PRIVACY - 2);
  public static final Address DEFAULT_PRIVACY_MANAGEMENT = Address.precompiled(PRIVACY - 3);

  public static final Address ZERO = Address.fromHexString("0x0");

  protected Address(final Bytes bytes) {
    super(bytes);
  }

  public static Address wrap(final Bytes value) {
    checkArgument(
        value.size() == SIZE,
        "An account address must be %s bytes long, got %s",
        SIZE,
        value.size());
    return new Address(value);
  }



  /**
   * Parse an hexadecimal string representing an account address.
   *
   * @param str An hexadecimal string (with or without the leading '0x') representing a valid
   *     account address.
   * @return The parsed address: {@code null} if the provided string is {@code null}.
   * @throws IllegalArgumentException if the string is either not hexadecimal, or not the valid
   *     representation of an address.
   */
  public static Address fromHexString(final String str) {
    if (str == null) return null;
    return wrap(Bytes.fromHexStringLenient(str, SIZE));
  }

  /**
   * Parse an hexadecimal string representing an account address.
   *
   * @param str An hexadecimal string representing a valid account address (strictly 20 bytes).
   * @return The parsed address.
   * @throws IllegalArgumentException if the provided string is {@code null}.
   * @throws IllegalArgumentException if the string is either not hexadecimal, or not the valid
   *     representation of a 20 byte address.
   */
  public static Address fromHexStringStrict(final String str) {
    checkArgument(str != null);
    final Bytes value = Bytes.fromHexString(str);
    checkArgument(
        value.size() == SIZE,
        "An account address must be be %s bytes long, got %s",
        SIZE,
        value.size());
    return new Address(value);
  }

  private static Address precompiled(final int value) {
    // Keep it simple while we don't need precompiled above 127.
    checkArgument(value < Byte.MAX_VALUE);
    final byte[] address = new byte[SIZE];
    address[SIZE - 1] = (byte) value;
    return new Address(Bytes.wrap(address));
  }
}
