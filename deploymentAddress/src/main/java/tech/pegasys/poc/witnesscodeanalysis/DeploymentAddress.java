package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.ContractData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static org.apache.logging.log4j.LogManager.getLogger;

public class DeploymentAddress {
  private static final Logger LOG = getLogger();

  public static final String FILE_NAME =  "analysis_deployaddress.json";

  private MainNetContractDataSet dataSet;

  Writer writer;
  Gson gson;
  int numDeployedContracts = 0;


  public DeploymentAddress() throws IOException {
    this.dataSet = new MainNetContractDataSet();

    this.writer = new FileWriter(FILE_NAME);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public void analyseAll() throws Exception {
    int count = 0;
    ContractData contractData;
    while ((contractData = this.dataSet.next()) != null) {
      contractData.showInfo(count);
      process(count, contractData);
      count++;
    }
    closeAll();
    LOG.info("Number of deployed contracts: {}", this.numDeployedContracts);
  }

  public void process(int id, ContractData contractData) throws IOException {
    String[] addresses = contractData.getContract_address();
    for (String address: addresses) {
      DeployAddressAndId data = new DeployAddressAndId(id, address);
      gson.toJson(data, this.writer);
      this.writer.append('\n');
      this.numDeployedContracts++;
    }
  }


  private void closeAll() throws IOException {
    this.dataSet.close();
    writer.close();
  }




  public static void main(String[] args) throws Exception {
    LOG.info("Deploy Address");
    DeploymentAddress analysis = new DeploymentAddress();
    analysis.analyseAll();
  }
}
