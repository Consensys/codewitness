package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.simple.PcUtils;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FixedSizeAnalysis extends CodeAnalysisBase {
  private static final Logger LOG = getLogger();
  private int threshold;

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public FixedSizeAnalysis(Bytes code, int threshold) {
    super(code);
    this.threshold = threshold;
  }

  public ArrayList<Integer> analyse() {
    int pc = 0;
    int currentChunkSize = 0;
    ArrayList<Integer> chunkStartAddresses = new ArrayList<>();
    chunkStartAddresses.add(0);

    while (pc != this.possibleEndOfCode) {

      final Operation curOp = registry.get(code.get(pc), 0);
      if (curOp == null) {
        LOG.error("Unknown opcode 0x{} at PC {}", Integer.toHexString(code.get(pc)), PcUtils.pcStr(pc));
        throw new Error("Unknown opcode");
      }
      int opSize = curOp.getOpSize();
      if (curOp.getOpcode() == 0) break;

      if(currentChunkSize + opSize >= threshold) {
        currentChunkSize = 0;
        pc += opSize;
        chunkStartAddresses.add(pc);
        continue;
      }

      currentChunkSize += opSize;
      pc += opSize;
    }

    return chunkStartAddresses;
    /*LOG.info("There are {} chunks with starting addresses : ", chunkStartAddresses.size());
    for(Integer e : chunkStartAddresses) {
      LOG.info(e);
    }*/
  }
}