package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.simple.AuxData;
import tech.pegasys.poc.witnesscodeanalysis.simple.SimpleAnalysis;

import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

public abstract class CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  Bytes code;
  AuxData auxData;
  int possibleEndOfCode;
  SimpleAnalysis simple;


  public CodeAnalysisBase(Bytes code) {
    this.code = code;
    this.auxData = new AuxData(code);
    this.possibleEndOfCode = code.size();
    if (this.auxData.hasAuxData()) {
      this.possibleEndOfCode = this.auxData.getStartOfAuxData();
    }
    this.simple = new SimpleAnalysis(code, this.possibleEndOfCode);
  }

  public void showBasicInfo() {
    LOG.info("Probably Solidity: {}", simple.isProbablySolidity());
    LOG.info("End of Function ID block: {}", simple.getEndOfFunctionIdBlock());
    LOG.info("End of Code: {}", simple.getEndOfCode());
    LOG.info("Offset Aux Data: {}", simple.getStartOfAuxData());
    LOG.info("Code Length: {}", code.size());
    if (auxData.hasAuxData()) {
      LOG.info("Compiler {} version {}", auxData.getCompilerName(), auxData.getCompilerVersion());
      LOG.info("Source Code stored in {}, message disgest of source code: {}", auxData.getSourceCodeStorageService(), auxData.getSourceCodeHash());
    }

    LOG.info("Functions found by simple scan");
    Set<Bytes> functionIds = simple.determineFunctionIds(simple.getEndOfCode());
    if (functionIds.size() == 0) {
      LOG.info(" No functions found");
    }
    for (Bytes functionId : functionIds) {
      LOG.info(" Function Id: {}", functionId);
    }
  }
}
