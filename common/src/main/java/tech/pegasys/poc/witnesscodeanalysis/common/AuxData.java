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
package tech.pegasys.poc.witnesscodeanalysis.common;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.poc.witnesscodeanalysis.vm.Code;

import static org.apache.logging.log4j.LogManager.getLogger;


/**
 * Should be something like:
 * 0xa2
 * 0x64 'i' 'p' 'f' 's' 0x58 0x22 <34 bytes IPFS hash>
 * 0x64 's' 'o' 'l' 'c' 0x43 <3 byte version encoding>
 * 0x00 0x33
 *
 *
 * The data is CBOR encoded: https://tools.ietf.org/html/rfc7049#page-7
 *
 * For Simple5.sol it is:
 * a265627a7a723058204d6870272cfd10a2f531d922cb7887d4b6e55a198a8ac353bfa03f569b33138064736f6c634300050a0032
 *
 * a2
 * 65          6X mean array of characters. 5 is the length
 * 627a7a7230
 *
 * 58
 * 20         length of message digest
 * 4d6870272cfd10a2f531d922cb7887d4b6e55a198a8ac353bfa03f569b331380  message digest
 *
 * 64        6X mean array of characters. 4 is the length
 * 736f6c63  "solc"
 *
 * 43        4X means array. 3 is length.
 * 00050a    00,05,0A = version 0.5.10
 *
 * 0032      The length of the aux data: 0x32 = 50 bytes
 */
public class AuxData {
  private static final Logger LOG = getLogger();

  private Bytes code;
  private boolean hasAuxData;
  private int startOfAuxData;
  private String sourceCodeStorageService;
  private Bytes sourceCodeHash;
  private String compilerName;
  private boolean isDefinitelySolidity;
  private Bytes compilerVersion;

  public AuxData(Bytes code) {
    this.code = code;
    analyse();
  }

  // TODO improve this analysis because it is flawed.
  private void analyse() {
    int len = this.code.size();
    if (len < 32) {
      this.hasAuxData = false;
      return;
    }
    byte b0 = code.get(len-2);
    byte b1 = code.get(len-1);
    this.hasAuxData = (b0 == 0) && ((b1 >= 0x29) && (b1 <= 0x33));
    if (!this.hasAuxData) {
      return;
    }
    this.startOfAuxData = len - b1 - 2;

    int ofs = this.startOfAuxData + 1;
    byte sourceCodeStorageService = this.code.get(ofs++);
    int lenSourceCodeStorageService = sourceCodeStorageService & 0xf;
    StringBuffer buffer = new StringBuffer();
    for (int i=0; i<lenSourceCodeStorageService; i++) {
      buffer.append((char)this.code.get(ofs++));
    }
    this.sourceCodeStorageService = buffer.toString();
    //System.out.println(this.sourceCodeStorageService);

    ofs++;
    int lenOfDigest = this.code.get(ofs++);
    if (lenOfDigest < 0) {
      LOG.info("Len of Digest is negative: {}", lenOfDigest);
      return;
    }
    if (lenOfDigest + ofs > this.code.size()) {
      LOG.info("Len of Digest is too large: {}", lenOfDigest);
      return;
    }
    this.sourceCodeHash = this.code.slice(ofs, lenOfDigest);
    ofs += lenOfDigest;

    int lenCompilerName = this.code.get(ofs++) & 0xf;
    buffer = new StringBuffer();
    for (int i=0; i<lenCompilerName; i++) {
      buffer.append((char)this.code.get(ofs++));
    }
    this.compilerName = buffer.toString();

    int lenCompilerVersion = this.code.get(ofs++) & 0xf;
    if (ofs + lenCompilerVersion > len) {
      return;
    }
    this.compilerVersion = this.code.slice(ofs, lenCompilerVersion);
  }

  public boolean hasAuxData() {
    return hasAuxData;
  }

  public int getStartOfAuxData() {
    return startOfAuxData;
  }

  public String getSourceCodeStorageService() {
    return sourceCodeStorageService;
  }

  public Bytes getSourceCodeHash() {
    return sourceCodeHash;
  }

  public String getCompilerName() {
    return compilerName;
  }

  public boolean isDefinitelySolidity() {
    return this.compilerName != null && this.compilerName.equalsIgnoreCase("solc");
  }

  public Bytes getCompilerVersion() {
    return compilerVersion;
  }
}
