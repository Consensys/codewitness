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

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.Special;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;


/**
 * Should be something like:
 * 0xa2                 The top three bits are the type: 101 indicates MAP. The bottom five
 *                      bits are the length. For map this is the number of key-value pairs.
 * 0x64 'i' 'p' 'f' 's' 0x58 0x22 <34 bytes IPFS hash>
 * 0x64 's' 'o' 'l' 'c' 0x43 <3 byte version encoding>
 * 0x00 0x33
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

  private static final String SWARM0 = "bzzr0";
  private static final String SWARM1 = "bzzr1";
  private static final String IPFS = "ipfs";
  private static final String SOLC = "solc";
  private static final String EXPERIMENTAL = "experimental";


  private Bytes code;
  private boolean hasAuxData;
  private int startOfAuxData;
  private String sourceCodeStorageService;
  private byte[] sourceCodeHash;
  private String compilerName;
  private boolean isDefinitelySolidity;
  private Bytes compilerVersion;
  private String experimentalInfo;

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
    int auxLength = (((((int)b0) << 8) & 0xff00) + (((int)b1) & 0xff));
    if (auxLength > len) {
      this.hasAuxData = false;
      return;
    }

    // Have a few extra rules to limit the amount of searching of non-aux data by the
    // CBOR decoder.
    // The Aux Data will be at least 32 (message digest size) and an indication of the
    // storage location (ipfs for example), plus a couple of bytes of formatting.
    // Assume the minimum length is 38 bytes
    if (auxLength < 39) {
      this.hasAuxData = false;
      return;
    }
    // Assume that aux data should never be more than... say 100 bytes.
    if (auxLength > 100) {
      this.hasAuxData = false;
      return;
    }
    int maybeStartOfAuxData = len - auxLength - 2;
    // Check that the first element is a map.
    byte firstByte = this.code.get(maybeStartOfAuxData);
    if (((firstByte >> 5) & 0x7) != 5) {
      this.hasAuxData = false;
      return;
    }
    // Check that the second byte, the key to the first item in the map, is a text string.
    byte secondByte = this.code.get(maybeStartOfAuxData+1);
    if (((secondByte >> 5) & 0x7) != 3) {
      this.hasAuxData = false;
      return;
    }

    Bytes auxDataEncoded = this.code.slice(maybeStartOfAuxData, auxLength);
    byte[] auxDataBytes = auxDataEncoded.toArray();

    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(auxDataBytes);
      List<DataItem> dataItems = new CborDecoder(bais).decode();
      for (DataItem dataItem: dataItems) {
        switch (dataItem.getMajorType()) {
          case MAP:
            Map map = (Map)dataItem;
            for (DataItem key: map.getKeys()) {
              DataItem val = map.get(key);
//              LOG.info("Key: {}, type: {}", key, key.getMajorType());
//              LOG.info("Value: {}, type: {}", val, val.getMajorType());
              if (key.getMajorType() == MajorType.UNICODE_STRING) {
                UnicodeString unicodeString = (UnicodeString) key;
                String keyStr = unicodeString.getString();
                if ((keyStr.equals(SWARM0) || keyStr.equals(SWARM1) || keyStr.equals(IPFS))) {
                  this.hasAuxData = true;
                  this.startOfAuxData = maybeStartOfAuxData;
                  this.sourceCodeStorageService = keyStr;
                  // Assume value major type is Byte String
                  ByteString byteString = (ByteString)val;
                  this.sourceCodeHash = byteString.getBytes();
                }
                else if (keyStr.equals(SOLC)) {
                  this.hasAuxData = true;
                  this.startOfAuxData = maybeStartOfAuxData;
                  this.isDefinitelySolidity = true;
                  this.compilerName = SOLC;
                  // Assume value major type is Byte String
                  ByteString byteString = (ByteString)val;
                  this.compilerVersion = Bytes.wrap(byteString.getBytes());
                }
                else if (keyStr.equals(EXPERIMENTAL)) {
                  this.hasAuxData = true;
                  this.startOfAuxData = maybeStartOfAuxData;
                  // Assume value major type is Special
                  Special special = (Special) val;
                  this.experimentalInfo = special.toString();
                }
                else {
                  LOG.error("Not implemented yet. Unknown Aux Data Key inside map: {}", keyStr);
                  throw new Error("Not implemented yet. Unknown Aux Data Key inside map: " + keyStr);
                }
              }
              else {
                LOG.error("Not implemented yet. Unknown Aux Data Key type in map: {}", key.getMajorType());
                throw new Error("Not implemented yet. Unknown Aux Data Key type in map: " + key.getMajorType());
              }


            }
            break;

          default:
            LOG.trace("Non-map found in aux data: probably not valid CBOR: {}", dataItem.getMajorType());
            this.hasAuxData = false;
            return;
        }
      }


    } catch (Exception ex) {
      this.hasAuxData = false;
      return;
    }


    this.hasAuxData = true;



//
//
//    int ofs = this.startOfAuxData + 1;
//    byte sourceCodeStorageService = this.code.get(ofs++);
//    int lenSourceCodeStorageService = sourceCodeStorageService & 0xf;
//    StringBuffer buffer = new StringBuffer();
//    for (int i=0; i<lenSourceCodeStorageService; i++) {
//      buffer.append((char)this.code.get(ofs++));
//    }
//    this.sourceCodeStorageService = buffer.toString();
//    //System.out.println(this.sourceCodeStorageService);
//
//    ofs++;
//    int lenOfDigest = this.code.get(ofs++);
//    if (lenOfDigest < 0) {
//      LOG.info("Len of Digest is negative: {}", lenOfDigest);
//      return;
//    }
//    if (lenOfDigest + ofs > this.code.size()) {
//      LOG.info("Len of Digest is too large: {}", lenOfDigest);
//      return;
//    }
//    this.sourceCodeHash = this.code.slice(ofs, lenOfDigest);
//    ofs += lenOfDigest;
//
//    int lenCompilerName = this.code.get(ofs++) & 0xf;
//    buffer = new StringBuffer();
//    for (int i=0; i<lenCompilerName; i++) {
//      buffer.append((char)this.code.get(ofs++));
//    }
//    this.compilerName = buffer.toString();
//
//    int lenCompilerVersion = this.code.get(ofs++) & 0xf;
//    if (ofs + lenCompilerVersion > len) {
//      return;
//    }
//    this.compilerVersion = this.code.slice(ofs, lenCompilerVersion);
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

  public byte[] getSourceCodeHash() {
    return sourceCodeHash;
  }

  public String getCompilerName() {
    return compilerName;
  }

  public boolean isDefinitelySolidity() {
    return this.isDefinitelySolidity;
  }

  public Bytes getCompilerVersion() {
    return compilerVersion;
  }

  public String getExperimentalInfo() {
    return experimentalInfo;
  }
}
