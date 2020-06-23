package tech.pegasys.poc.witnesscodeanalysis;

public class DeployAddressAndId {
  private int id;
  private String contract_address;

  public DeployAddressAndId(final int id, final String contractAddress) {
    this.id = id;
    this.contract_address = contractAddress;
  }


  public int getId() {
    return id;
  }

  public String getContract_address() {
    return contract_address;
  }
}
