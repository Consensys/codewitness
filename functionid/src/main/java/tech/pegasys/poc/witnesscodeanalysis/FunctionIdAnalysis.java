package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractByteCode;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FunctionIdAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  public FunctionIdAnalysis(Bytes code) {
    super(code);
  }

  public void runFunctionIdProcess() {
    FunctionIdProcess analysis = new FunctionIdProcess(this.code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
    analysis.executeAnalysis();
  }


  public static void main(String[] args) throws Exception {
    LOG.info("TODO: add support for contract data.");

    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0x6475593a8c52aac4059b1eb68235004f136eda5d);

    FunctionIdAnalysis analysis = new FunctionIdAnalysis(code);
    analysis.showBasicInfo();
    analysis.runFunctionIdProcess();
 }
}
