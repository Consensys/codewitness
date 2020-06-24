package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DeployDataSetReader {
  private static Map<String, Integer> contractsToId = null;

  //  private static final Logger LOG = getLogger();

  private BufferedReader reader;
  private Gson gson;


  public DeployDataSetReader() throws IOException {
    this(DeploymentAddress.FILE_NAME);
  }

  public DeployDataSetReader(String fileIn) throws IOException {
    Path pathToFileIn = Paths.get(fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    this.reader = reader;

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public DeployAddressAndId next() throws IOException {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return gson.fromJson(line, DeployAddressAndId.class);
    } catch (IOException ioe) {
//      throw ioe;
      return null;
    }
  }

  public void close() throws IOException {
    reader.close();
  }

  public static Map<String, Integer> getContractsToId() throws IOException {
    if (contractsToId == null) {
      contractsToId = new HashMap<>();
      DeployDataSetReader dataSetReader = new DeployDataSetReader();
      DeployAddressAndId info;
      try {
        while ((info = dataSetReader.next()) != null) {
          contractsToId.put(info.getContract_address(), info.getId());
        }
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    }
    return contractsToId;
  }

}
