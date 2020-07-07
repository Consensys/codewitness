package tech.pegasys.poc.witnesscodeanalysis.trace.alethio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AlethioDataSetReader {
  private Gson gson;


  public AlethioDataSetReader() {
    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
//    this.gson = new GsonBuilder().create();
  }


  public AlethioPageOfTransactions parse(String line) {
    return gson.fromJson(line, AlethioPageOfTransactions.class);
  }
}
