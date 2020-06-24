package tech.pegasys.poc.witnesscodeanalysis.tracecombiner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

public class TraceCombiner {
  public static final String DIR_FROM = "traces1";
  public static final String DIR_TO = "traces2";

  Set<Integer> alreadyProcessed = new TreeSet<>();

  public void run() throws Exception {
    File f = new File(DIR_FROM);
    File[] matchingFiles = f.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return true;
      }
    });

    for (File aFile: matchingFiles) {
      System.out.println(aFile.toString());
      System.out.println(aFile.isDirectory());

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

    for (File aFile: matchingFiles) {
      String fileName = aFile.getName();
      String blockNumberStr = fileName.substring(0, 7);

//      System.out.println(aFile.getName());
//      System.out.println(blockNumberStr);

      int blockNumber = Integer.valueOf(blockNumberStr);
      if (this.alreadyProcessed.contains(blockNumber)) {
        continue;
      }

      File[] matchingFiles1 = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.startsWith(blockNumberStr);
        }
      });
      String combined = "";
      for (File file: matchingFiles1) {
        System.out.println(" Processing file: " + file.toString());
        try {
          BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.US_ASCII);
          String line = reader.readLine();
          reader.close();
          combined += line;
        } catch (Exception ex) {
          System.out.println(ex.getMessage());
        }
      }
      this.alreadyProcessed.add(blockNumber);


      String outputFile = DIR_TO + "/trace" + blockNumberStr + ".json";
      BufferedWriter writer = Files.newBufferedWriter((new File(outputFile)).toPath());
      writer.write(combined);
      writer.close();
    }


  }





  public static void main(String[] args) throws Exception {
    (new TraceCombiner()).run();
  }
}
