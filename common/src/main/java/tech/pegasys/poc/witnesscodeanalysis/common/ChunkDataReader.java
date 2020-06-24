package tech.pegasys.poc.witnesscodeanalysis.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChunkDataReader {
  private BufferedReader reader;
  private Gson gson;

  public ChunkDataReader(String fileIn) throws IOException {
    Path pathToFileIn = Paths.get(fileIn);
    BufferedReader reader = Files.newBufferedReader(pathToFileIn, StandardCharsets.US_ASCII);
    this.reader = reader;

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public ChunkData next() {
    try {
      String line = reader.readLine();
      if (line == null) {
        return null;
      }
      return gson.fromJson(line, ChunkData.class);
    } catch (IOException ioe) {
      return null;
    }
  }

  public void close() throws IOException {
    reader.close();
  }
}
