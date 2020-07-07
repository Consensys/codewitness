package tech.pegasys.poc.witnesscodeanalysis.combined.duplicateanalysis;

public class WitnessResultDuplicate {
  public int blockNumber;
  public int unique = 0; //functionIdWitnessSizeUniqueCode
  public int peraddr = 0; // functionIdWitnessSizePerAddress
  public int unknown;  // Unknown contract count
  public int fail = 0; // failContractNotProcessedCorrectlyByFunctionIdAnalysis
  public int total = 0; // total number of contracts.

  public WitnessResultDuplicate(int blockNumber, int total, int unknownContractCount) {
    this.blockNumber = blockNumber;
    this.total = total;
    this.unknown = unknownContractCount;
  }
}
