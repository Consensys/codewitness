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
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessException;
import tech.pegasys.poc.witnesscodeanalysis.common.UnableToProcessReason;
import tech.pegasys.poc.witnesscodeanalysis.vm.operations.CodeCopyOperation;

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



  CodePaths codePaths;

  public FunctionIdProcess(Bytes code, int endOfFunctionIdBlock, int endOfCode, Set<Integer> jumpDests) {
    this.code = code;
    this.endOfFunctionIdBlock = endOfFunctionIdBlock;
    this.endOfCode = endOfCode;
    this.jumpDests = jumpDests;
    CodeCopyOperation.setConsumer(CodeCopyConsumer.getInstance());
  }


  public void executeAnalysis(FunctionIdAllResult result) {
    this.codePaths = new CodePaths(this.code, this.jumpDests);
    codePaths.findFunctionBlockCodePaths(this.endOfFunctionIdBlock);
    codePaths.findCodeSegmentsForFunctions();

    codePaths.showAllCodePaths();
    // Validate Code Segments throws an exception if there is an issue.
    this.codePaths.validateCodeSegments(this.endOfCode);

    int COMBINATION_GAP = 0;
    LOG.trace("Combining Code Segments using bytes between segments: {}", COMBINATION_GAP);
    codePaths.combineCodeSegments(COMBINATION_GAP);

    CodeCopyOperation.removeConsumer();
    createMerklePatriciaTrieLeaves(result);
  }

  public static void addAllCodeLeaf(FunctionIdAllResult result, Bytes code1) {
    // Add in a leaf with all code. It has an invalid function id that is 5 bytes long.
    Map<Integer, Integer> allCode = new TreeMap<>();
    allCode.put(0, code1.size());
    FunctionIdMerklePatriciaTrieLeafData allCodeLeaf = new FunctionIdMerklePatriciaTrieLeafData(CodeVisitor.ALL_CODE_FUNCTIONID, code1, allCode);
    result.addLeaf(allCodeLeaf);
  }

  private void createMerklePatriciaTrieLeaves(FunctionIdAllResult result) {
    // Map of function id to Map of start to length.
    Map<Bytes, Map<Integer, Integer>> allCombinedCodeBlocks = this.codePaths.getAllCombinedCodeBlocks();

    // Add in a leaf with all code. It has an invalid function id that is 5 bytes long.
    addAllCodeLeaf(result, this.code);

    for (Bytes functionId: allCombinedCodeBlocks.keySet()) {
      Map<Integer, Integer> startLen = allCombinedCodeBlocks.get(functionId);
      FunctionIdMerklePatriciaTrieLeafData leaf = new FunctionIdMerklePatriciaTrieLeafData(functionId, code, startLen);
      result.addLeaf(leaf);
      LOG.trace("FunctionId: {}, Leaf size: {}", functionId, leaf.getEncodedLeaf().length);
    }
  }
}
