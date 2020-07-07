package tech.pegasys.poc.witnesscodeanalysis.trace.fs;

public class FsBlock {
  long blockNumber;
  FsTransaction[] transactions;

  public FsBlock(long blockNumber, FsTransaction[] transactions) {
    this.blockNumber = blockNumber;
    this.transactions = transactions;
  }

  public long getBlockNumber() {
    return this.blockNumber;
  }

  public FsTransaction[] getTransactions() {
    return this.transactions;
  }



}
