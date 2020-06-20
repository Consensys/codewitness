package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.logging.log4j.Logger;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeCopyOperation;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.logging.log4j.LogManager.getLogger;

class CodeCopyConsumer implements CodeCopyOperation.BasicBlockConsumer {
  private static final Logger LOG = getLogger();

  private static CodeCopyConsumer instance = new CodeCopyConsumer();

  private Map<Integer, BasicBlockWithCode> blocks;


  public static CodeCopyConsumer getInstance() {
    return instance;
  }

  private CodeCopyConsumer() {
    this.blocks = new TreeMap();
  }


  public void reset() {
    this.blocks = new TreeMap();
  }


  @Override
  public void addNewBlock(BasicBlockWithCode block) {
    BasicBlockWithCode existing = this.blocks.get(block.getStart());
    if (existing != null) {
      if (existing.getLength() != block.getLength()) {
        LOG.info("******** A code copy block was inserted with a different length to the existing block");
        LOG.info("Existing: Start: {}, Length: {}", existing.getStart(), existing.getLength());
        LOG.info("New: Start: {}, Length: {}", block.getStart(), block.getLength());
      }
    }
    else {
      blocks.put(block.getStart(), block);
    }
  }

  public Map<Integer, BasicBlockWithCode> getBlocks() {
    return this.blocks;
  }

}
