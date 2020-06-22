package tech.pegasys.poc.witnesscodeanalysis.functionid;

import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;

import java.util.ArrayList;

public class FunctionIdAllResult {
  private int id;
  private String[] deployedAddresses;
  private UnableToProcessReason result;
  private boolean isProbablySolidity;
  private boolean isNewSolidity;

  private ArrayList<FunctionIdMerklePatriciaTrieLeafData> leaves = new ArrayList<>();

  public void setContractInfo(int id, String[] deployedAddresses) {
    this.id = id;
    this.deployedAddresses = deployedAddresses;
  }

  public void addLeaf(FunctionIdMerklePatriciaTrieLeafData leaf) {
    this.leaves.add(leaf);
  }

  public void setOverallResult(UnableToProcessReason result) {
    this.result = result;
  }

  public void setSolidityInfo(boolean probablySolidity, boolean newSolidity) {
    this.isProbablySolidity = probablySolidity;
    this.isNewSolidity = newSolidity;
  }

  public ArrayList<FunctionIdMerklePatriciaTrieLeafData> getLeaves() {
    return leaves;
  }
}
