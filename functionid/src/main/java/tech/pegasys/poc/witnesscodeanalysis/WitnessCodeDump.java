package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.simple.ByteCodePrinter;

import static org.apache.logging.log4j.LogManager.getLogger;

public class WitnessCodeDump extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();

  public WitnessCodeDump(Bytes code) {
    super(code);
  }

  public void dumpContract() {
    ByteCodePrinter printer = new ByteCodePrinter(this.code);
    printer.print(0, this.simple.getEndOfCode() + 1);
  }


  public static void main(String[] args) {
    Bytes code = Bytes.fromHexString(ContractByteCode.contract_0x63de3096c22e89f175c8ed51ca0c129118516979);

    WitnessCodeDump dump = new WitnessCodeDump(code);
    dump.showBasicInfo();
    dump.dumpContract();
  }
}
