package apachebeamtraining;

import java.io.Serializable;

public class CryptoCurrency implements Serializable {

  private String name;
  private double amount;

  public CryptoCurrency(String name, double amount) {
    this.name = name;
    this.amount = amount;
  }

  public double getAmount() {
    return amount;
  }

  public String toString() {
    return String.format("name:%s amount:%.2f", name, amount);
  }
}
