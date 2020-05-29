package tech.pegasys.poc.witnesscodeanalysis;

import java.util.List;

public class ContractData {

  private String code;
  private String code_hash;
  private Integer code_length;
  private List<String> contract_address;
  private List<Integer> deployed_at_block;
  private List<Integer> deployed_at_unix;
  private List<Integer> recent_accessed_at_block;
  private List<Integer> recent_accessed_at_unix;
  private String solidity_metadata;

  public String getCode() {
    return code;
  }

  public String getCode_hash() {
    return code_hash;
  }

  public Integer getCode_length() {
    return code_length;
  }

  public List<String> getContract_address() {
    return contract_address;
  }

  public List<Integer> getDeployed_at_block() {
    return deployed_at_block;
  }

  public List<Integer> getDeployed_at_unix() {
    return deployed_at_unix;
  }

  public List<Integer> getRecent_accessed_at_block() {
    return recent_accessed_at_block;
  }

  public List<Integer> getRecent_accessed_at_unix() {
    return recent_accessed_at_unix;
  }

  public String getSolidity_metadata() {
    return solidity_metadata;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setCode_hash(String code_hash) {
    this.code_hash = code_hash;
  }

  public void setCode_length(Integer code_length) {
    this.code_length = code_length;
  }

  public void setContract_address(List<String> contract_address) {
    this.contract_address = contract_address;
  }

  public void setDeployed_at_block(List<Integer> deployed_at_block) {
    this.deployed_at_block = deployed_at_block;
  }

  public void setDeployed_at_unix(List<Integer> deployed_at_unix) {
    this.deployed_at_unix = deployed_at_unix;
  }

  public void setRecent_accessed_at_block(List<Integer> recent_accessed_at_block) {
    this.recent_accessed_at_block = recent_accessed_at_block;
  }

  public void setRecent_accessed_at_unix(List<Integer> recent_accessed_at_unix) {
    this.recent_accessed_at_unix = recent_accessed_at_unix;
  }

  public void setSolidity_metadata(String solidity_metadata) {
    this.solidity_metadata = solidity_metadata;
  }
}
