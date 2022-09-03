# 6. MultiOutput: Failure Handling

1. Create `Failure`
    ```java
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
    ```
1. Create `MultiOutput`:
    - Input: `PCollection<KV<String, Iterable<CryptoCurrency>>>`
    - Output: `PCollectionTuple` with `TupleTag<String>` `validTag` and `failuresTag`

    ```java
    package apachebeamtraining;

    import java.util.Date;
    import java.util.Iterator;
    import org.apache.beam.sdk.transforms.DoFn;
    import org.apache.beam.sdk.transforms.ParDo;
    import org.apache.beam.sdk.values.KV;
    import org.apache.beam.sdk.values.PCollection;
    import org.apache.beam.sdk.values.PCollectionTuple;
    import org.apache.beam.sdk.values.TupleTag;
    import org.apache.beam.sdk.values.TupleTagList;

    public class MultipleOutput extends DoFn<KV<String, Iterable<CryptoCurrency>>, String> {
      public static final TupleTag<String> validTag = new TupleTag<String>() {};
      public static final TupleTag<String> failuresTag = new TupleTag<String>() {};


      public static PCollectionTuple process(PCollection<KV<String, Iterable<CryptoCurrency>>> batch) {
        return batch.apply("Create PubSub objects",
            ParDo.of(new DoFn<KV<String, Iterable<CryptoCurrency>>, String>() {
              @ProcessElement
              public void processElement(ProcessContext c) {
                KV<String, Iterable<CryptoCurrency>> batch = c.element();
                try {
                  int count = 0;
                  Iterator<CryptoCurrency> iter = batch.getValue().iterator();

                  // this will fail when iter has elements less than 3 -> check failure handling
                  for (int i = 0; i < 3; i++) {
                    iter.next();
                    count++;
                  }

                  BatchResult batchResult =
                      new BatchResult(batch.getKey(), count, new Date(System.currentTimeMillis()));
                  c.output(batchResult.toString());

                } catch (Throwable throwable) {
                  Failure failure = new Failure(batch, batch, throwable);
                  c.output(failuresTag, failure.toString());
                }

              }
            }).withOutputTags(validTag, TupleTagList.of(failuresTag)));
      }
    }
    ```

1. Pipeline:

    ```java
    PCollectionTuple outputTuple = MultipleOutput.process(batchedCrypt);
    PCollection<String> success = outputTuple.get(MultipleOutput.validTag);
    PCollection<String> failure = outputTuple.get(MultipleOutput.failuresTag);
    success.apply(TextIO.write().to("output-success"));
    failure.apply(TextIO.write().to("output-failure"));
    ```
1. Run the pipeline.
    ```
    ./gradlew run
    ```
1. Check results:
    Failure:
    - `data:KV{ETH/JPY, [name:ETH/JPY amount:1126316.00]}`
    - `message:java.util.NoSuchElementException`
    ```
    cat app/output-failure-00000-of-00001
    failedClass:class org.apache.beam.sdk.values.KV,message:java.util.NoSuchElementException,data:KV{ETH/JPY, [name:ETH/JPY amount:1126316.00]},stackTrace:[java.base/java.util.ArrayList$Itr.next(ArrayList.java:1000), apachebeamtraining.MultipleOutput$3.processElement(MultipleOutput.java:30), apachebeamtraining.MultipleOutput$3$DoFnInvoker.invokeProcessElement(Unknown Source), org.apache.beam.repackaged.direct_java.runners.core.SimpleDoFnRunner.invokeProcessElement(SimpleDoFnRunner.java:211), org.apache.beam.repackaged.direct_java.runners.core.SimpleDoFnRunner.processElement(SimpleDoFnRunner.java:188), org.apache.beam.repackaged.direct_java.runners.core.SimplePushbackSideInputDoFnRunner.processElementInReadyWindows(SimplePushbackSideInputDoFnRunner.java:79), org.apache.beam.runners.direct.ParDoEvaluator.processElement(ParDoEvaluator.java:244), org.apache.beam.runners.direct.DoFnLifecycleManagerRemovingTransformEvaluator.processElement(DoFnLifecycleManagerRemovingTransformEvaluator.java:54), org.apache.beam.runners.direct.DirectTransformExecutor.processElements(DirectTransformExecutor.java:165), org.apache.beam.runners.direct.DirectTransformExecutor.run(DirectTransformExecutor.java:129), java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515), java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264), java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128), java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628), java.base/java.lang.Thread.run(Thread.java:829)]
    ```

    Success: 13 x 3 = 39 out of 41 (input records) successfully processed.
    ```
    cat app/output-success-0000*
    key:ETH/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:ETH/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:ETH/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:ETH/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:BTC/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    key:ETH/JPY, count:3, completedAt:2022-09-03 at 10:31:058 JST
    ```
