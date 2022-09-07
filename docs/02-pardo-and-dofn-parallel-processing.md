# [2. ParDo and DoFn: Parallel processing](https://github.com/nakamasato/apache-beam-training/tree/3f436ec8ba2c5295485cb25a3f744ac209e6c5c7)

- [ParDo](https://beam.apache.org/documentation/programming-guide/#pardo): a Beam transform for generic parallel processing.
- **DoFn**: When you apply a ParDo transform, you’ll need to provide user code in the form of a DoFn object. DoFn is a Beam SDK class that defines a distributed processing function.

1. Change the input data `app/input-record.txt`.

    ```
    BTC/JPY,bitflyer,1519845731987,1127174.0,1126166.0
    BTC/JPY,bitflyer,1519845742363,1127470.0,1126176.0
    BTC/JPY,bitflyer,1519845752427,1127601.0,1126227.0
    BTC/JPY,bitflyer,1519845762038,1127591.0,1126316.0
    BTC/JPY,bitflyer,1519845772637,1127801.0,1126368.0
    BTC/JPY,bitflyer,1519845782073,1126990.0,1126411.0
    BTC/JPY,bitflyer,1519845792827,1127990.0,1126457.0
    BTC/JPY,bitflyer,1519845802008,1127980.0,1126500.0
    BTC/JPY,bitflyer,1519845812088,1127980.0,1126566.0
    BTC/JPY,bitflyer,1519845822743,1127970.0,1126601.0
    ```

1. Add `transform` step with `ParDo` to the pipeline.
    ```java
    PCollection<String> textData = p.apply(TextIO.read().from("input-record.txt"));

    PCollection<String> bidData = textData.apply(ParDo.of(new ExtractBid()));

    bidData.apply(TextIO.write().to("output")); // just changed the name of PCollection and the prefix of output files
    ```
1. Define `DoFn` `ExtractBid`.

    1. extends `DoFn<String, String>` (`<String, String>` means the input and the output are both `String`)
    1. [`@ProcessElement` annotation](https://beam.apache.org/releases/javadoc/2.3.0/org/apache/beam/sdk/transforms/DoFn.ProcessElement.html) is necessary.

    ```java
    public static class ExtractBid extends DoFn<String, String> {
        @ProcessElement
        public void process(ProcessContext c){

            String row = c.element();

            String[] cells = row.split(",");
            c.output(cells[4]);
        }
    }
    ```

1. Run the app

    ```
    ./gradlew run
    ```

    You'll see output files `output-*` with only the 5th field as their contents. (Multiple files are generated as they are processed in parallel.)
