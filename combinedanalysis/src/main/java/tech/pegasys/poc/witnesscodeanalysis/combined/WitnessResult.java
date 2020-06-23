package tech.pegasys.poc.witnesscodeanalysis.combined;

public class WitnessResult {
  public int blockNumber;
  public int functionIdWitnessSize = 0;
  public int jumpDestWitnessSize = 0;
  public int fixedWitnessSize = 0;
  public int strictWitnessSize = 0;

  public WitnessResult(int blockNumber) {
    this.blockNumber = blockNumber;
  }


}
