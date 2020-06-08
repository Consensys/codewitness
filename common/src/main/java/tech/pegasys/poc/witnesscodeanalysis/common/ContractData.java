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

/**
 * Class to match fields from data file.
 */
public class ContractData {

  private String code;
  private String code_hash;
  private int code_length;
  private String[] contract_address;
  private int[] deployed_at_block;
  private int[] deployed_at_unix;
  private int[] recent_accessed_at_block;
  private int[] recent_accessed_at_unix;
  private String solidity_metadata;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getCode_hash() {
    return code_hash;
  }

  public void setCode_hash(String code_hash) {
    this.code_hash = code_hash;
  }

  public int getCode_length() {
    return code_length;
  }

  public void setCode_length(int code_length) {
    this.code_length = code_length;
  }

  public String[] getContract_address() {
    return contract_address;
  }

  public void setContract_address(String[] contract_address) {
    this.contract_address = contract_address;
  }

  public int[] getDeployed_at_block() {
    return deployed_at_block;
  }

  public void setDeployed_at_block(int[] deployed_at_block) {
    this.deployed_at_block = deployed_at_block;
  }

  public int[] getDeployed_at_unix() {
    return deployed_at_unix;
  }

  public void setDeployed_at_unix(int[] deployed_at_unix) {
    this.deployed_at_unix = deployed_at_unix;
  }

  public int[] getRecent_accessed_at_block() {
    return recent_accessed_at_block;
  }

  public void setRecent_accessed_at_block(int[] recent_accessed_at_block) {
    this.recent_accessed_at_block = recent_accessed_at_block;
  }

  public int[] getRecent_accessed_at_unix() {
    return recent_accessed_at_unix;
  }

  public void setRecent_accessed_at_unix(int[] recent_accessed_at_unix) {
    this.recent_accessed_at_unix = recent_accessed_at_unix;
  }

  public String getSolidity_metadata() {
    return solidity_metadata;
  }

  public void setSolidity_metadata(String solidity_metadata) {
    this.solidity_metadata = solidity_metadata;
  }
}
