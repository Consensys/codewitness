package tech.pegasys.poc.witnesscodeanalysis.combined.duplicateanalysis;

public class WitnessResultDuplicate {
  public int blockNumber;
  public int functionIdWitnessSizeUniqueCode = 0;
  public int functionIdWitnessSizePerAddress = 0;

  public WitnessResultDuplicate(int blockNumber) {
    this.blockNumber = blockNumber;
  }


}
