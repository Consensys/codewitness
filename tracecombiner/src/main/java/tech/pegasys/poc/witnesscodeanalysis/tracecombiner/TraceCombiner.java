package tech.pegasys.poc.witnesscodeanalysis.tracecombiner;

import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioPageOfTransactions;
import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioTransaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TraceCombiner {
  public static final String DIR_FROM = "traces1";
  public static final String DIR_TO = "traces2";

  AlethioDataSetReader dataSetReader = new AlethioDataSetReader();

  public void run() throws Exception {
    File f = new File(DIR_FROM);
    File[] matchingFiles = f.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return true;
      }
    });

    for (File aFile: matchingFiles) {
      System.out.println(aFile.toString());
      if (aFile.isDirectory()) {
        processDirectory(aFile);
      }
    }

  }

  public void processDirectory(File dir) throws Exception {
    File[] matchingFiles = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return true;
      }
    });

    for (File file: matchingFiles) {
      String fileName = file.toString();
      if (fileName.endsWith(".DS_Store")) {
        System.out.println(" Skipping file: " + fileName);
        continue;
      }
      System.out.println(" Processing file: " + fileName);
      StringBuffer buffer = new StringBuffer();
      try {
        BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
        String line = reader.readLine();
        while (line != null) {
          buffer.append(line);
          buffer.append("\n");
          line = reader.readLine();
        }
        reader.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println(ex.getMessage());
      }

      AlethioPageOfTransactions pageOfTransactions = dataSetReader.parse(buffer.toString());
//      System.out.println("Number of transactions: " +  pageOfTransactions.data.length);
      for (AlethioTransaction tx: pageOfTransactions.data) {
        long blockNumber = tx.attributes.globalRank[0];
        String txHash = tx.attributes.txHash;
        String functionSelector = tx.attributes.msgPayload.funcSelector;
        String to = tx.relationships.to.data.id;
//        System.out.println(
//            " Block" + blockNumber +
//            ", Transaction id " + tx.attributes.globalRank[1] +
//            ", TxHash: " + txHash +
//            ", Function Selector: " + functionSelector +
//            ", To: " + to);

        final int FILES_PER_DIR = 1000;
        long blockGroup = (blockNumber / FILES_PER_DIR) * FILES_PER_DIR;
        String outputPath = DIR_TO + "/block" + blockGroup;
        Files.createDirectories(Paths.get(outputPath));

        String outputFile = outputPath + "/trace" + blockNumber + ".json";
        File outFile = new File(outputFile);
        BufferedWriter writer;
        if (outFile.exists()) {
          writer = Files.newBufferedWriter(outFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }
        else {
          writer = Files.newBufferedWriter(outFile.toPath(), StandardOpenOption.CREATE);
        }


//TODO this needs to be JSON Format!!!!!

        writer.write("TODO");
        writer.close();

      }

    }


  }





  public static void main(String[] args) throws Exception {
    (new TraceCombiner()).run();
  }
}
