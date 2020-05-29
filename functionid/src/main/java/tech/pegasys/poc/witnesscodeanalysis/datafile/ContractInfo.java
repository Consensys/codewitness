package tech.pegasys.poc.witnesscodeanalysis.datafile;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class ContractInfo {
  String code;
  String code_hash;
  int code_length;
  String[] contract_address;
  long[] deployed_at_block;
  long[] deployed_at_unix_time;
  String[] recent_accessed_at_block;   // Needs to be a string because some values are "NA"
  String[] recent_accessed_at_unix;    // Needs to be a string because some values are "NA"



  public Bytes getCode() {
    return Bytes.fromHexString(this.code);
  }

  public Bytes32 getCodeHash() {
    return Bytes32.fromHexString(this.code_hash);
  }

  public int getCodeLength() {
    return this.code_length;
  }

  public String[] getContractAddresses() {
    return this.contract_address;
  }





}
