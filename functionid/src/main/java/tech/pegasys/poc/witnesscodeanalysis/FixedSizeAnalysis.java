package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.InvalidOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.JumpDestOperation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FixedSizeAnalysis {
  private static final Logger LOG = getLogger();

  public static OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

  public ArrayList<Integer> analyse(int threshold, Bytes code) {
    int pc = 0;
    int currentChunkSize = 0;
    ArrayList<Integer> chunkStartAddresses = new ArrayList<>();
    chunkStartAddresses.add(0);

    while (true) {

      final Operation curOp = registry.get(code.get(pc), 0);
      int opSize = curOp.getOpSize();
      int opCode = curOp.getOpcode();

      if(currentChunkSize + opSize >= threshold) {
        currentChunkSize = 0;
        pc += opSize;
        chunkStartAddresses.add(pc);
        continue;
      }

      if (opCode == InvalidOperation.OPCODE) {
        //LOG.info("Reached END");
        break;
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