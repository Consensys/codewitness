package tech.pegasys.poc.witnesscodeanalysis.datafile;

import com.google.gson.Gson;

public class ContractJsonProcessor {
  Gson gson = new Gson();


  public ContractInfo processEntry(String json) {
    // from JSON to object
    return this.gson.fromJson(json, ContractInfo.class);
  }


}
