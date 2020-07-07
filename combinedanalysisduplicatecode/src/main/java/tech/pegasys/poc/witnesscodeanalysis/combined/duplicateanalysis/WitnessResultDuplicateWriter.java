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

  public static final String FILE_NAME =  "analysis_witness_duplicate.json";
  private Writer writer;
  private Gson gson;


  public WitnessResultDuplicateWriter() throws IOException {
    this.writer = new FileWriter(FILE_NAME);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public void writeResult(WitnessResultDuplicate result) throws IOException {
    gson.toJson(result, this.writer);
    this.writer.append('\n');
  }

  public void flush() throws IOException {
    this.writer.flush();
  }
  public void close() throws IOException {
    this.writer.close();
  }
}
