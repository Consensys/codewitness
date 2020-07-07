package tech.pegasys.poc.witnesscodeanalysis.trace.fs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.pegasys.poc.witnesscodeanalysis.trace.dataset8m.TraceBlockData;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FunctionSelectorDataSetReader {

  //  private static final Logger LOG = getLogger();

  private BufferedReader reader;
  private Gson gson;


  public FunctionSelectorDataSetReader(final int blockNumber) throws IOException {
    this(blockNumberToFileLocation(blockNumber));
  }

  public static Path blockNumberToFileLocation(final long blockNumber) {
    final int FILES_PER_DIR = 1000;
    long blockGroup = (blockNumber / FILES_PER_DIR) * FILES_PER_DIR;
    String path = "traces/block" + blockGroup + "/trace" + blockNumber + ".json";
    return Paths.get(path);
  }

  public FunctionSelectorDataSetReader(Path pathToFileIn) throws IOException {
    this.reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public FsBlock read() {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return gson.fromJson(line, FsBlock.class);
    } catch (IOException ioe) {
      return null;
    }
  }

  public void close() throws IOException {
    this.reader.close();
  }

}
