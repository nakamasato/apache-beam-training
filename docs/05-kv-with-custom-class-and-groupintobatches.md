# 5. KV with custom class and GroupIntoBatches

- [GroupIntoBatches](https://beam.apache.org/releases/javadoc/2.0.0/org/apache/beam/sdk/transforms/GroupIntoBatches.html): A PTransform that batches inputs to a desired batch size. Batches will contain only elements of a single key.

1. Create `CryptoCurrency`

    ```java
    package apachebeamtraining;

    import java.io.Serializable;

    public class CryptoCurrency implements Serializable {

      private String name;
      private long amount;

      public CryptoCurrency(String name, long amount) {
        this.name = name;
        this.amount = amount;
      }

      public long getAmount() {
        return amount;
      }

      public String toString() {
        return String.format("name:%s amount:%d", name, amount);
      }
    }
    ```

1. Convert text into `KV<String, CryptoCurrency>`

    ```java
    /*
     * String -> KV<String, CryptoCurrency>
     */
    static class ConvertTextIntoKVCryptoCurrencyFn
            extends DoFn<String, KV<String, CryptoCurrency>> {
        @ProcessElement
        public void processElement(@Element String row,
                OutputReceiver<KV<String, CryptoCurrency>> receiver) {
            String[] cells = row.split(",");
            try {
                double amount = Double.parseDouble(cells[4]);
                receiver.output(KV.of(cells[0], new CryptoCurrency(cells[0], amount)));
            } catch (NumberFormatException ex) {
                logger.error("failed to convert to CryptoCurrency", ex);
            } catch (ArrayIndexOutOfBoundsException ex) {
                logger.error("failed to convert to CryptoCurrency", ex);
            }
        }
    }
    ```

    ```java
    PCollection<KV<String, CryptoCurrency>> cryptoKV =
            textData.apply(ParDo.of(new ConvertTextIntoKVCryptoCurrencyFn()));
    ```

1. Prepare `BatchResult`
    ```java
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
    ```
1. Group into batches.
    ```java
    PCollection<KV<String, Iterable<CryptoCurrency>>> batchedCrypt =
            cryptoKV.apply(GroupIntoBatches.<String, CryptoCurrency>ofSize(3));
    ```
1. Process batches (`KV<String, Iterable<CryptoCurrency>>`)

    ```java
    /*
     * Process item in a batch. Just count the number of item and return BatchResult as String.
     */
    static class ProcessBatch extends DoFn<KV<String, Iterable<CryptoCurrency>>, String> {
        @ProcessElement
        public void process(OutputReceiver<String> receiver,
                @Element KV<String, Iterable<CryptoCurrency>> batch) {
            int count = 0;
            Iterator<CryptoCurrency> iter = batch.getValue().iterator();
            while (iter.hasNext()) {
                count++;
            }
            receiver.output(String.valueOf(count));
        }
    }
    ```

    ```java
    batchedCrypt.apply(ParDo.of(new ProcessBatch())).apply(TextIO.write().to("output-count"));
    ```

1. Run `./gradlew run`

    You can confirm the batch process result:
    ```
    cat app/output-count-*
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:ETH/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:055 JST
    key:ETH/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:ETH/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:ETH/JPY, count:3, completedAt:2022-09-02 at 16:20:055 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:055 JST
    key:ETH/JPY, count:3, completedAt:2022-09-02 at 16:20:055 JST
    key:BTC/JPY, count:3, completedAt:2022-09-02 at 16:20:054 JST
    key:ETH/JPY, count:1, completedAt:2022-09-02 at 16:20:055 JST
    ```
