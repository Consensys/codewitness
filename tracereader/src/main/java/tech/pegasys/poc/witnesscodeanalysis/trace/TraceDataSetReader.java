package tech.pegasys.poc.witnesscodeanalysis.trace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TraceDataSetReader {

  //  private static final Logger LOG = getLogger();

  private BufferedReader reader;
  private Gson gson;


  public TraceDataSetReader(final int blockNumber) throws IOException {
    this("trace" + blockNumber + ".json");
  }

  public TraceDataSetReader(String fileIn) throws IOException {
    Path pathToFileIn = Paths.get("traces/", fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    this.reader = reader;

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public TraceBlockData next() {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return gson.fromJson(line, TraceBlockData.class);
    } catch (IOException ioe) {
      return null;
    }
  }

  public void close() throws IOException {
    reader.close();
  }

}
