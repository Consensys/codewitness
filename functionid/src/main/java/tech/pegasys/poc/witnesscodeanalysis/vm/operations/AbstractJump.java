package tech.pegasys.poc.witnesscodeanalysis.vm.operations;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.poc.witnesscodeanalysis.vm.AbstractOperation;

import static org.apache.logging.log4j.LogManager.getLogger;

public abstract class AbstractJump extends AbstractOperation {
  private static final Logger LOG = getLogger();


  public AbstractJump(
      final int opcode,
      final String name,
      final int stackItemsConsumed,
      final int stackItemsProduced,
      final int opSize) {
    super(
        opcode,
        name,
        stackItemsConsumed,
        stackItemsProduced,
        opSize);
  }


  protected void checkJumpDest(UInt256 jumpDest) {
    UInt256 result = jumpDest.and(UInt256.valueOf(DYNAMIC_MARKER_MASK)).subtract(DYNAMIC_MARKER);
    if (result.isZero()) {
      LOG.error("********Dynamic Jump Found: {}", jumpDest);
    }
  }
}
