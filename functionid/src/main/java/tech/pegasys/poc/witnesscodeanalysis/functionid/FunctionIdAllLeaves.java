package tech.pegasys.poc.witnesscodeanalysis.functionid;

import java.util.ArrayList;

public class FunctionIdAllLeaves {
  private ArrayList<FunctionIdMerklePatriciaTrieLeafData> leaves = new ArrayList<>();

  public void addLeaf(FunctionIdMerklePatriciaTrieLeafData leaf) {
    this.leaves.add(leaf);
  }

  public ArrayList<FunctionIdMerklePatriciaTrieLeafData> getLeaves() {
    return leaves;
  }
}
