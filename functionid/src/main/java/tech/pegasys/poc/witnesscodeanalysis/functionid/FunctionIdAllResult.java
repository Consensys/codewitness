package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;

import java.util.ArrayList;

public class FunctionIdAllResult {
  private int id;
  private UnableToProcessReason result;
  private boolean isProbablySolidity;
  private boolean isNewSolidity;

  private ArrayList<FunctionIdMerklePatriciaTrieLeafData> leaves = new ArrayList<>();

  public void setContractInfo(int id) {
    this.id = id;
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

  // TODO leaves need to be in a map!
  public FunctionIdMerklePatriciaTrieLeafData getLeaf(Bytes functionId) {
    for (FunctionIdMerklePatriciaTrieLeafData leaf: this.leaves) {
      if (functionId.compareTo(leaf.getFunctionId()) == 0) {
        return leaf;
      }
    }
    return null;
  }

  public int getId() {
    return id;
  }

  public UnableToProcessReason getResult() {
    return result;
  }

  public boolean isProbablySolidity() {
    return isProbablySolidity;
  }

  public boolean isNewSolidity() {
    return isNewSolidity;
  }
}
