package tech.pegasys.poc.witnesscodeanalysis.simple;

public class PcUtils {
  public static String pcStr(int pc) {
    return "0x" + Integer.toHexString(pc) + " (" + pc + ")";
  }

}
