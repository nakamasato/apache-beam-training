# 7. MultiOutput with different types

In the last section [6. MultiOutput: Failure Handling](06-multioutput-failure-handling.md), `MultipleOutput` returned the same type `String`:
```java
public static final TupleTag<String> validTag = new TupleTag<String>() {};
public static final TupleTag<String> failuresTag = new TupleTag<String>() {};
```

In this lecture, we'll return different types.


1. Change `TupleTag` types to `BatchResult` and `Failure` for `validTag` and `failureTag` respectively.

    ```java
    public static final TupleTag<BatchResult> validTag = new TupleTag<BatchResult>() {};
    public static final TupleTag<Failure> failureTag = new TupleTag<Failure>() {};
    ```

1. Update all relevant places in `MultiOutput`.
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

    public class MultipleOutput extends DoFn<KV<String, Iterable<CryptoCurrency>>, BatchResult> {
      public static final TupleTag<BatchResult> validTag = new TupleTag<BatchResult>() {};
      public static final TupleTag<Failure> failureTag = new TupleTag<Failure>() {};


      public static PCollectionTuple process(PCollection<KV<String, Iterable<CryptoCurrency>>> batch) {
        return batch.apply("Create PubSub objects",
            ParDo.of(new DoFn<KV<String, Iterable<CryptoCurrency>>, BatchResult>() {
              @ProcessElement
              public void processElement(ProcessContext c, MultiOutputReceiver out) {
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
                  out.get(validTag).output(batchResult);

                } catch (Throwable throwable) {
                  Failure failure = new Failure(batch, batch, throwable);
                  out.get(failureTag).output(failure);
                }

              }
            }).withOutputTags(validTag, TupleTagList.of(failureTag)));
      }
    }
    ```


1. Add `ConvertIntoStringFn` to `App.java`

    ```java
    /**
     * T -> String
     */
    static class ConvertToStringFn<T> extends DoFn<T, String> {
      @ProcessElement
      public void processElement(ProcessContext c) {
        c.output(String.valueOf(c.element()));
      }
    }
    ```
1. Update the pipeline with `ConvertToStringFn`.
    1. Process1

        ```java
        // Process1: Group by the cryptocurrency name and write each line length to the file.
        PCollection<KV<String, Integer>> mapped = textData.apply(ParDo.of(new ConvertStringIntoKVFn()));
        PCollection<KV<String, Iterable<Integer>>> groupByKey =
            mapped.apply(GroupByKey.<String, Integer>create());
        PCollection<String> count =
            groupByKey.apply(ParDo.of(new ConvertToStringFn<KV<String, Iterable<Integer>>>()));
        count.apply(TextIO.write().to("output-aggregated"));
        ```

    1. Process4
        ```java
        // Process4: Use MultipleOutput
        PCollectionTuple outputTuple = MultipleOutput.process(batchedCrypt);
        PCollection<BatchResult> success = outputTuple.get(MultipleOutput.validTag);
        PCollection<Failure> failure = outputTuple.get(MultipleOutput.failureTag);
        success.apply(ParDo.of(new ConvertToStringFn<BatchResult>()))
            .apply(TextIO.write().to("output-success"));
        failure.apply(ParDo.of(new ConvertToStringFn<Failure>()))
            .apply(TextIO.write().to("output-failure"));
        ```
1. Fix test with `ConvertToStringFn`
    ```java
        PCollection<String> count = groupByKey.apply(ParDo.of(new App.ConvertToStringFn<KV<String, Iterable<Integer>>>()));
    ```
1. Run `./gradlew run` -> failure

    ```
    SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
    SLF4J: Defaulting to no-operation (NOP) logger implementation
    SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
    Exception in thread "main" java.lang.IllegalStateException: Unable to return a default Coder for Create PubSub objects.out0 [PCollection@710220387]. Correct one of the following root causes:
      No Coder has been manually specified;  you may do so using .setCoder().
      Inferring a Coder from the CoderRegistry failed: Unable to provide a Coder for apachebeamtraining.BatchResult.
      Building a Coder using a registered CoderProvider failed.
      See suppressed exceptions for detailed failures.
      Using the default output Coder from the producing PTransform failed: PTransform.getOutputCoder called.
            at org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkState(Preconditions.java:507)
            at org.apache.beam.sdk.values.PCollection.getCoder(PCollection.java:286)
            at org.apache.beam.sdk.values.PCollection.finishSpecifying(PCollection.java:117)
            at org.apache.beam.sdk.runners.TransformHierarchy.finishSpecifyingInput(TransformHierarchy.java:154)
            at org.apache.beam.sdk.Pipeline.applyInternal(Pipeline.java:547)
            at org.apache.beam.sdk.Pipeline.applyTransform(Pipeline.java:482)
            at org.apache.beam.sdk.values.PCollection.apply(PCollection.java:363)
            at apachebeamtraining.App.main(App.java:140)
    ```
1. 
