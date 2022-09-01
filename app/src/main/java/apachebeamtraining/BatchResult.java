package apachebeamtraining;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BatchResult {
  private int count;
  private String key;
  private Date completedAt;
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:sss z");

  public BatchResult(String key, int count, Date completedAt) {
    this.key = key;
    this.count = count;
    this.completedAt = completedAt;
  }

  public String toString() {
    return String.format("key:%s, count:%d, completedAt:%s", key, count,
        formatter.format(completedAt));
  }
}
