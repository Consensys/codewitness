package tech.pegasys.poc.witnesscodeanalysis.combined.duplicateanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessResultDuplicateWriter {
  private static final Logger LOG = getLogger();

  public static final String FILE_NAME =  "analysis_witness_duplicate.";
  public static final String JSON = "json";
  public static final String CSV = "csv";
  private Writer writer;
  private Gson gson;

  private boolean useJson;


  public WitnessResultDuplicateWriter(boolean json) throws IOException {
    String fileName = json ? FILE_NAME + JSON : FILE_NAME + CSV;
    this.writer = new FileWriter(fileName);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();

    this.useJson = json;
  }


  public void writeResult(WitnessResultDuplicate result) throws IOException {
    if (this.useJson) {
      gson.toJson(result, this.writer);
      this.writer.append('\n');
    }
    else {
      String line =
          result.blockNumber + "," +
          result.unique + "," +
          result.peraddr + "," +
          result.total + "," +
          result.unknown + "," +
          result.fail + "\n";
      this.writer.write(line);
    }
  }

  public void flush() throws IOException {
    this.writer.flush();
  }
  public void close() throws IOException {
    this.writer.close();
  }
}
