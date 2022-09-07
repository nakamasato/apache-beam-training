package apachebeamtraining;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BatchResult implements Serializable {
  private int count;
  private String key;
  private Date completedAt;

  public BatchResult(String key, int count, Date completedAt) {
    this.key = key;
    this.count = count;
    this.completedAt = completedAt;
  }

  public String toString() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:sss z");
    return String.format("key:%s, count:%d, completedAt:%s", key, count,
        formatter.format(completedAt));
  }
}
