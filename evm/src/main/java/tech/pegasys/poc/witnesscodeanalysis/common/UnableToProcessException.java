package tech.pegasys.poc.witnesscodeanalysis.common;

public class UnableToProcessException extends RuntimeException {
  String msg;
  UnableToProcessReason reason;

  public UnableToProcessException() {
  }

  public UnableToProcessException(String message) {
    super(message);
  }

  public UnableToProcessException(UnableToProcessReason reason, String message) {
    super("Unable to process " + reason + ", " + message);
    this.reason = reason;
    this.msg = message;
  }

  public UnableToProcessException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnableToProcessException(Throwable cause) {
    super(cause);
  }

  protected UnableToProcessException(String message, Throwable cause,
                             boolean enableSuppression,
                             boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }


  public String getMsg() {
    return msg;
  }

  public UnableToProcessReason getReason() {
    return reason;
  }
}
