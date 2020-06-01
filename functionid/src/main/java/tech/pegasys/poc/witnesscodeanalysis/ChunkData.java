package tech.pegasys.poc.witnesscodeanalysis;

import java.util.ArrayList;

public class ChunkData {
  ArrayList<Integer> chunkStartAddresses;

  ChunkData(ArrayList<Integer> chunkStartAddresses) {
    this.chunkStartAddresses = chunkStartAddresses;
  }
}
