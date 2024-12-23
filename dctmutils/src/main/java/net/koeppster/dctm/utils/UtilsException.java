package net.koeppster.dctm.utils;

public class UtilsException extends Exception {

  private static final long serialVersionUID = 1L;

  public UtilsException() {
    super();
  }

  public UtilsException(String message) {
    super(message);
  }

  public UtilsException(String message, Throwable cause) {
    super(message, cause);
  }

  public UtilsException(Throwable cause) {
    super(cause);
  }

  protected UtilsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
