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
package tech.pegasys.poc.witnesscodeanalysis.processing;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.poc.witnesscodeanalysis.common.ChunkData;
import tech.pegasys.poc.witnesscodeanalysis.strictfixed.StrictFixedSizeAnalysis;

import java.io.IOException;
import java.util.ArrayList;

import static org.apache.logging.log4j.LogManager.getLogger;

public class StrictFixedSizeProcessing extends AbstractProcessing {
  private static final Logger LOG = getLogger();

  public static final String DEFAULT_NAME =  "strictfixed";

  private int threshold;

  public StrictFixedSizeProcessing(boolean json) throws IOException {
    this(json, THRESHOLD);
  }

  public StrictFixedSizeProcessing(boolean json, int threshold) throws IOException {
    super(DEFAULT_NAME, json);
    this.threshold = threshold;
  }

  @Override
  protected void executeProcessing(int id, String[] deployedAddresses, Bytes code) throws Exception {
    ArrayList<Integer> chunkStartOffsets;
    ChunkData chunkData;

    LOG.trace(" StrictFixedSize Analysis started");
    chunkStartOffsets = new StrictFixedSizeAnalysis(code, this.threshold).analyse();
    LOG.trace("  Finished. {} chunks", chunkStartOffsets.size());
    chunkData = new ChunkData(id, chunkStartOffsets, code, true, this.threshold);

    if (this.json) {
      gson.toJson(chunkData, this.writer);
      this.writer.append('\n');
    }
    else {
      throw new Error("NOT IMPLEMENTED YET");
    }
  }
}
