/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.poc.witnesscodeanalysis.functionid;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.BasicBlockWithCode;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcess;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessException;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeCopyOperation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.apache.logging.log4j.LogManager.getLogger;

public class FunctionIdProcess {
  private static final Logger LOG = getLogger();

  Bytes code;
  int endOfFunctionIdBlock;
  int endOfCode;
  Set<Integer> jumpDests;

  CodeCopyConsumer codeCopyBlocksConsumer;


  CodePaths codePaths;

  public FunctionIdProcess(Bytes code, int endOfFunctionIdBlock, int endOfCode, Set<Integer> jumpDests) {
    this.code = code;
    this.endOfFunctionIdBlock = endOfFunctionIdBlock;
    this.endOfCode = endOfCode;
    this.jumpDests = jumpDests;

    this.codeCopyBlocksConsumer = new CodeCopyConsumer();
    CodeCopyOperation.setConsumer(this.codeCopyBlocksConsumer);
  }


  public FunctionIdAllLeaves executeAnalysis() {
    UnableToProcess unableToProcessInstance = UnableToProcess.getInstance();
    unableToProcessInstance.clean();

    FunctionIdAllLeaves leaves = null;
    try {
      this.codePaths = new CodePaths(this.code, this.jumpDests);
      codePaths.findFunctionBlockCodePaths(this.endOfFunctionIdBlock);
      codePaths.findCodeSegmentsForFunctions();

      codePaths.showAllCodePaths();
      boolean codePathsValid = codePaths.validateCodeSegments(this.endOfCode);
      LOG.trace("Code Paths Valid: {}", codePathsValid);
      if (!codePathsValid) {
        return null;
      }

      int COMBINATION_GAP = 4;
      LOG.trace("Combining Code Segments using bytes between segments: {}", COMBINATION_GAP);
      codePaths.combineCodeSegments(COMBINATION_GAP);

      Collection<BasicBlockWithCode> blocks = this.codeCopyBlocksConsumer.getBlocks();
      if (blocks != null) {
        LOG.info("******** num copy Code blocks: {}", blocks.size());
        for (BasicBlockWithCode block: blocks) {
          LOG.info("  block: start: {}, len: {}", block.getStart(), block.getLength());
        }
      }

      leaves = createMerklePatriciaTrieLeaves();
    } catch (UnableToProcessException ex) {
      LOG.info(" Unable to Process: {}: {}", unableToProcessInstance.getReason(), unableToProcessInstance.getMessage());
    }
    CodeCopyOperation.removeConsumer();
    return leaves;
  }


  private FunctionIdAllLeaves createMerklePatriciaTrieLeaves() {
    // Map of function id to Map of start to length.
    Map<Bytes, Map<Integer, Integer>> allCombinedCodeBlocks = this.codePaths.getAllCombinedCodeBlocks();
    FunctionIdAllLeaves leaves = new FunctionIdAllLeaves();

    for (Bytes functionId: allCombinedCodeBlocks.keySet()) {
      Map<Integer, Integer> startLen = allCombinedCodeBlocks.get(functionId);
      FunctionIdMerklePatriciaTrieLeafData leaf = new FunctionIdMerklePatriciaTrieLeafData(functionId, code, startLen);
      leaves.addLeaf(leaf);
      LOG.trace("FunctionId: {}, Leaf size: {}", functionId, leaf.getEncodedLeaf().length);
    }
    return leaves;
  }


  class CodeCopyConsumer implements CodeCopyOperation.BasicBlockConsumer {
    Map<Integer, BasicBlockWithCode> blocks;

    CodeCopyConsumer() {
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

    public Collection<BasicBlockWithCode> getBlocks() {
      return this.blocks.values();
    }

  }

}
