package tech.pegasys.poc.witnesscodeanalysis.tracecombiner;

import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioPageOfTransactions;
import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.alethio.AlethioTransaction;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FsBlock;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FsTransaction;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FunctionSelectorDataSetReader;
import tech.pegasys.poc.witnesscodeanalysis.trace.fs.FunctionSelectorDataSetWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TraceCombiner {
  public static final String DIR_FROM = "traces1";
  public static final String TEMP_DIR1 = DIR_FROM + "/temp1";
  public static final String TEMP_DIR2 = DIR_FROM + "/temp2";
  public static final String DIR_TO = "traces2";

  int numProcessed = 0;

  AlethioDataSetReader dataSetReader = new AlethioDataSetReader();

  public void run() throws Exception {
    File f = new File(DIR_FROM);
    File[] matchingFiles = f.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".zip");
      }
    });

    for (File topLevelZipFile: matchingFiles) {
      processTopLevelZipFile(topLevelZipFile);
    }
  }

  private void processTopLevelZipFile(File topLevelZipFile) throws Exception {
    File tempDir = new File(TEMP_DIR1);
    Files.createDirectories(tempDir.toPath());
    unzip(topLevelZipFile, tempDir, true);

    File[] matchingFiles = tempDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".zip");
      }
    });
    for (File secondLevelZipFile: matchingFiles) {
      processSecondLevelZipFile(secondLevelZipFile);
    }
    deleteAllFilesInDirectory(tempDir);
  }

  private void processSecondLevelZipFile(File secondLevelZipFile) throws Exception {
    File tempDir = new File(TEMP_DIR2);
    Files.createDirectories(tempDir.toPath());
    unzip(secondLevelZipFile, tempDir, true);
    processDirectory(tempDir);
    deleteAllFilesInDirectory(tempDir);
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
      System.out.println(" " + this.numProcessed + ": Processing file: " + fileName);
      this.numProcessed++;
      processFile(file.toPath());
    }
  }

  public void processFile(Path file) throws Exception {
    StringBuffer buffer = new StringBuffer();
    try {
      BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
      String line = reader.readLine();
      while (line != null) {
        buffer.append(line);
        line = reader.readLine();
      }
      reader.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }

    AlethioPageOfTransactions pageOfTransactions = dataSetReader.parse(buffer.toString());
//      System.out.println("Number of transactions: " +  pageOfTransactions.data.length);
    AlethioTransaction[] alethioTransactions = pageOfTransactions.data;
    long blockNumber = alethioTransactions[0].attributes.globalRank[0];
    ArrayList<FsTransaction> transactions = new ArrayList<>();
    for (AlethioTransaction alethioTransaction: alethioTransactions) {
      long blockNumberCurrentTransaction = alethioTransaction.attributes.globalRank[0];
      String txHash = alethioTransaction.attributes.txHash;
      String functionSelector = alethioTransaction.attributes.msgPayload.funcSelector;
      String to = alethioTransaction.relationships.to.data.id;
      FsTransaction fsTransaction = new FsTransaction(txHash, to, functionSelector);

//        System.out.println(
//            " Block" + blockNumber +
//            ", Transaction id " + alethioTransaction.attributes.globalRank[1] +
//            ", TxHash: " + txHash +
//            ", Function Selector: " + functionSelector +
//            ", To: " + to);

      if (blockNumberCurrentTransaction == blockNumber) {
        transactions.add(fsTransaction);
      } else {
        writeTransactionToFile(blockNumber, transactions);

        // Now set-up for the next block.
        transactions.clear();
        transactions.add(fsTransaction);
        blockNumber = blockNumberCurrentTransaction;
      }
    }
    writeTransactionToFile(blockNumber, transactions);
  }


  public void writeTransactionToFile(long blockNumber, ArrayList<FsTransaction> transactions) throws Exception {
    final int FILES_PER_DIR = 1000;
    long blockGroup = (blockNumber / FILES_PER_DIR) * FILES_PER_DIR;
    String outputPath = DIR_TO + "/block" + blockGroup;
    Files.createDirectories(Paths.get(outputPath));

    String outputFile = outputPath + "/trace" + blockNumber + ".json";
    File outFile = new File(outputFile);
    if (outFile.exists()) {
      FunctionSelectorDataSetReader functionSelectorDataSetReader = new FunctionSelectorDataSetReader(outFile.toPath());
      FsBlock block = functionSelectorDataSetReader.read();
      functionSelectorDataSetReader.close();
      transactions.addAll(Arrays.asList(block.getTransactions()));
    }

    FsTransaction[] transactionsArray = transactions.toArray(new FsTransaction[]{});
    FsBlock fsBlock = new FsBlock(blockNumber, transactionsArray);
    FunctionSelectorDataSetWriter functionSelectorDataSetWriter = new FunctionSelectorDataSetWriter(outFile.toPath());
    functionSelectorDataSetWriter.write(fsBlock);
    functionSelectorDataSetWriter.close();
  }




  private static void unzip(File zipFile, File destDir, boolean removeZipDirs) throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      if (zipEntry.isDirectory()) {
        zipEntry = zis.getNextEntry();
        continue;
      }
      File newFile = newFile(destDir, zipEntry, removeZipDirs);
      FileOutputStream fos = new FileOutputStream(newFile);
      int len;
      while ((len = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      fos.close();
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
  }

  private static File newFile(File destinationDir, ZipEntry zipEntry, boolean removeZipDirs) throws IOException {
    String zipEntryName = zipEntry.getName();
    if (removeZipDirs) {
      int lastPath = zipEntryName.lastIndexOf('/');
      if (lastPath != -1) {
        zipEntryName = zipEntryName.substring(lastPath+1);
      }
    }

    File destFile = new File(destinationDir, zipEntryName);

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  private static void deleteAllFilesInDirectory(File directory) throws IOException {
    for(File file: directory.listFiles()) {
      if (!file.isDirectory()) {
        file.delete();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    (new TraceCombiner()).run();
  }
}
