package apachebeamtraining;

import java.util.Arrays;

public class Failure {
  private String failedClass;
  private String message;
  private String precursorDataString;
  private String stackTrace;

  public Failure(Object precursorData, Object datum, Throwable thrown) {
    this.failedClass = datum.getClass().toString();
    this.message = thrown.toString();
    this.precursorDataString = precursorData.toString();
    this.stackTrace = Arrays.toString(thrown.getStackTrace());
  }

  public String toString() {
    return String.format("failedClass:%s,message:%s,data:%s,stackTrace:%s", failedClass, message,
        precursorDataString, stackTrace);
  }
}
