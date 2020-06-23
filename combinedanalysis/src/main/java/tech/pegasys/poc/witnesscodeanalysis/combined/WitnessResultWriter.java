package tech.pegasys.poc.witnesscodeanalysis.combined;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkData;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkDataReader;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.functionid.CodeVisitor;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdAllResult;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdMerklePatriciaTrieLeafData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessResultWriter {
  private static final Logger LOG = getLogger();

  public static final String FILE_NAME =  "analysis_witness.json";
  private Writer writer;
  private Gson gson;


  public WitnessResultWriter() throws IOException {
    this.writer = new FileWriter(FILE_NAME);

    //  Parsing the JSON file for contract code
    this.gson = new GsonBuilder().setLenient().create();
  }


  public void writeResult(WitnessResult result) throws IOException {
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
