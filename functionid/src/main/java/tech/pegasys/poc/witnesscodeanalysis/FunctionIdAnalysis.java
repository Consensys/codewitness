package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.datafile.ContractByteCode;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FunctionIdAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  public static final String c2 = "6080604052348015600f57600080fd5b506004361060285760003560e01c8063e4e38de314602d575b600080fd5b605160048036036020811015604157600080fd5b50356001600160f81b0319166053565b005b60008190808054603f811680603e8114608457600283018455600183166077578192505b600160028404019350609c565b600084815260209081902060ff198516905560419094555b505050906001820381546001161560c25790600052602060002090602091828204019190065b90919290919091601f036101000a81548160ff02191690600160f81b84040217905550505056fea265627a7a72305820cbefd4b0dee9a80b2e9504d0ee0be3c1678689daca48affa0911c4498ab6871864736f6c634300050a0032";

  public FunctionIdAnalysis(Bytes code) {
    super(code);
  }

  public void runFunctionIdProcess() {
    FunctionIdProcess analysis = new FunctionIdProcess(this.code, simple.getEndOfFunctionIdBlock(), simple.getEndOfCode(), simple.getJumpDests());
    analysis.executeAnalysis();
  }


  public static void main(String[] args) throws Exception {
    LOG.info("TODO: add support for contract data.");

//    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0x6475593a8c52aac4059b1eb68235004f136eda5d);
    Bytes code = Bytes.fromHexString(c2);

    FunctionIdAnalysis analysis = new FunctionIdAnalysis(code);
    analysis.showBasicInfo();
    analysis.runFunctionIdProcess();
 }
}
