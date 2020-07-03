package tech.pegasys.poc.witnesscodeanalysis.trace.alethio;

public class AlethioTransactionPayload {

  public String funcName;
  public String funcSelector;
  public String funcSignature;
  public String funcDefinition;
  public AlethioFunctionParameter[] inputs;
  public AlethioFunctionParameter[] outputs;
  public String raw;
}
