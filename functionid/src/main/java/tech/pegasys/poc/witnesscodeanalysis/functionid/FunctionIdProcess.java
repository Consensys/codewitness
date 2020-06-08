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
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeCopyOperation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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

    ArrayList<BasicBlockWithCode> blocks = this.codeCopyBlocksConsumer.getBlocks();
    if (blocks != null) {
      LOG.info("******** num copy Code blocks: {}", blocks.size());
      for (BasicBlockWithCode block: blocks) {
        LOG.info("  block: start: {}, len: {}", block.getStart(), block.getLength());
      }

    }


    CodeCopyOperation.removeConsumer();

    return createMerklePatriciaTrieLeaves();
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
    ArrayList<BasicBlockWithCode> blocks;

    CodeCopyConsumer() {
      this.blocks = new ArrayList<>();
    }


    @Override
    public void addNewBlock(BasicBlockWithCode block) {
      blocks.add(block);
    }

    public ArrayList<BasicBlockWithCode> getBlocks() {
      return this.blocks;
    }

  }

}
