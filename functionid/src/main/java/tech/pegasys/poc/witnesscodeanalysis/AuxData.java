package tech.pegasys.poc.witnesscodeanalysis;

import org.apache.tuweni.bytes.Bytes;

public class AuxData {

  public static boolean hasAuxData(Bytes code) {
    return offsetOfAuxData(code) != 0;
  }

  public static int offsetOfAuxData(Bytes code) {
    int len = code.size();
    byte b0 = code.get(len-2);
    byte b1 = code.get(len-1);
    if ((b0 == 0) && ((b1 == 0x32) || (b1 == 0x33))) {
      return len - b1 - 2;
    }
    return 0;
  }

}
