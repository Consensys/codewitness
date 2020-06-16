package tech.pegasys.poc.witnesscodeanalysis.common;

public class UnableToProcessException extends RuntimeException {
  public UnableToProcessException() {
  }

  public UnableToProcessException(String message) {
    super(message);
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
}
