package tech.pegasys.poc.witnesscodeanalysis.trace.fs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FunctionSelectorDataSetWriter {

  //  private static final Logger LOG = getLogger();

  private BufferedWriter writer;
  private Gson gson;


  public FunctionSelectorDataSetWriter(final int blockNumber) throws IOException {
    this("trace" + blockNumber + ".json");
  }

  public FunctionSelectorDataSetWriter(String fileIn) throws IOException {
    this(Paths.get("traces/", fileIn));
  }

  public FunctionSelectorDataSetWriter(Path pathToFileIn) throws IOException {
    this.writer = Files.newBufferedWriter(pathToFileIn, StandardCharsets.US_ASCII);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public void write(FsBlock block) throws IOException {
    String line = this.gson.toJson(block);
    this.writer.write(line);
  }

  public void close() throws IOException {
    this.writer.close();
  }

}
