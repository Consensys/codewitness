package tech.pegasys.poc.witnesscodeanalysis.common;

import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.LogManager.getLogger;

public class UnableToProcess {
  private static final Logger LOG = getLogger();

  private static UnableToProcess instance = new UnableToProcess();

  private UnableToProcessReason reason;
  private String message;

  public static UnableToProcess getInstance() {
    return instance;
  }

  public void unableToProcess(UnableToProcessReason reason) {
    unableToProcess(reason, "");
  }

  public void unableToProcess(UnableToProcessReason reason, String message) {
    this.reason = reason;
    this.message = message;
    throw new UnableToProcessException(reason, message);
  }

  public void clean() {
    this.reason = null;
    this.message = null;
  }

  public UnableToProcessReason getReason() {
    return reason;
  }

  public String getMessage() {
    return message;
  }
}
