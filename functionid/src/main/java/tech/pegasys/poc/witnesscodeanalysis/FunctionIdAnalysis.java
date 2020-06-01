package tech.pegasys.poc.witnesscodeanalysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.functionid.FunctionIdProcess;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import static java.lang.System.exit;
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

    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0xd94ea6e43b7bffc9e4cba93f3ca49a191dc06d90);

    FunctionIdAnalysis analysis = new FunctionIdAnalysis(code);
    analysis.showBasicInfo();
    analysis.runFunctionIdProcess();
 }
}
