package tech.pegasys.poc.witnesscodeanalysis.functionid;

import java.util.ArrayList;

public class FunctionIdAllLeaves {
  private int id;
  private String deployedAddress;

  private ArrayList<FunctionIdMerklePatriciaTrieLeafData> leaves = new ArrayList<>();

  public void setContractInfo(int id, String deployedAddress) {
    this.id = id;
    this.deployedAddress = deployedAddress;
  }

  public void addLeaf(FunctionIdMerklePatriciaTrieLeafData leaf) {
    this.leaves.add(leaf);
  }

  public ArrayList<FunctionIdMerklePatriciaTrieLeafData> getLeaves() {
    return leaves;
  }
}
