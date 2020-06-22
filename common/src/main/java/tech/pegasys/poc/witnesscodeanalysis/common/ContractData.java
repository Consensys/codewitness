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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Class to match fields from data file.
 */
public class ContractData {
  private static final Logger LOG = getLogger();

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


  public void showInfo(int contractNumber) {
    String contractAddress = getContract_address()[0];
    int numDeployments = getContract_address().length;

    // Print out information about the contract to get a feel for how important the results are.
    LOG.info("Processing contract {} deployed at address: {} and {} other times", contractNumber, contractAddress, numDeployments - 1);
    if (LOG.getLevel() == Level.TRACE) {
      LOG.trace(" Code Size: " + this.code.length() / 2);
      int firstDeployment = 100000000;
      int[] deployments = getDeployed_at_block();
      for (int j = 0; j < numDeployments; j++) {
        if (deployments[j] < firstDeployment) {
          firstDeployment = deployments[j];
        }
      }
      LOG.trace(" First Deployed at block: " + firstDeployment);
      int lastTransaction = 0;
      int[] lastTransactions = getRecent_accessed_at_block();
      if (lastTransactions != null) {
        for (int j = 0; j < numDeployments; j++) {
          if (lastTransactions[j] > lastTransaction) {
            lastTransaction = lastTransactions[j];
          }
        }
        LOG.trace(" Last transaction for all deployments: {}", lastTransaction);
      }
    }
  }
}
