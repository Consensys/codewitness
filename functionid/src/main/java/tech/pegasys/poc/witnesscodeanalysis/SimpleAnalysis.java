package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.vm.MainnetEvmRegistries;
import tech.pegasys.poc.witnesscodeanalysis.vm.Operation;
import tech.pegasys.poc.witnesscodeanalysis.vm.OperationRegistry;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.MStoreOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.PushOperation;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.RevertOperation;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class SimpleAnalysis {
  Bytes code;

  public SimpleAnalysis(Bytes code) {
    this.code = code;
  }

  public boolean probablySolidity() {
    int len = code.size();
    if (len < 10) {
      return false;
    }

    // Look for:
    //    PUSH1 0x80
    //    PUSH1 0x40
    //    MSTORE
    return
        ((code.get(0) == (byte) PushOperation.PUSH1_OPCODE) &&
            (code.get(1) == (byte)0x80) &&
            (code.get(2) == (byte)PushOperation.PUSH1_OPCODE) &&
            (code.get(3) == (byte)0x40) &&
            (code.get(4) == (byte) MStoreOperation.OPCODE) );
  }



  Set<Bytes> determineFunctionIds(int probableEndOfCode) {
    Set<Bytes> functionIds = new HashSet<>();

    OperationRegistry registry = MainnetEvmRegistries.berlin(BigInteger.ONE);

    int pc = 0;

    // Go until the call data is loaded.
    boolean done = false;
    while (!done) {
      Operation curOp = registry.get(code.get(pc), 0);
      if (curOp.getName().equalsIgnoreCase("CALLDATALOAD")) {
        done = true;
      }
      pc = pc + curOp.getOpSize();
      if (pc > probableEndOfCode) {
        throw new Error("No REVERT found in code");
      }
    }

    // The next section contains the function ids. Keep going until the revert is encountered
    done = false;
    while (!done) {
      Operation curOp = registry.get(code.get(pc), 0);
      if (curOp.getOpcode() == RevertOperation.OPCODE) {
        done = true;
      }
      else if (curOp.getOpcode() == PushOperation.PUSH4_OPCODE) {
        Bytes functionId = code.slice(pc+1, 4);
        functionIds.add(functionId);
      }
      pc = pc + curOp.getOpSize();
      if (pc > probableEndOfCode) {
        throw new Error("No REVERT found in code");
      }
    }
    return functionIds;
  }



}
