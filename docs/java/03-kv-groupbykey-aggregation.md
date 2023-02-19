# [3. KV + GroupByKey: Aggregation](https://github.com/nakamasato/apache-beam-training/tree/dae6d17099ae3e66da6672983b4aba5dc14a3bbf)

- [KV](https://beam.apache.org/releases/javadoc/2.2.0/index.html?org/apache/beam/sdk/values/KV.html): An immutable key value pair
- [GroupByKey](https://beam.apache.org/documentation/programming-guide/#groupbykey): a Beam transform for processing collections of key/value pairs.


1. Convert `PCollection<Stringe>` into [KV](https://beam.apache.org/releases/javadoc/2.0.0/org/apache/beam/sdk/values/KV.html) with **SimpleFunction** ([Simple case of DoFn](https://stackoverflow.com/questions/50525766/apache-beam-what-is-the-difference-between-dofn-and-simplefunction)). (use the first column for the key)

    ```java
    PCollection<KV<String, Integer>> mapped =
    textData.apply(MapElements.via(new SimpleFunction<String, KV<String, Integer>>() {
        @Override
        public KV<String, Integer> apply(String line) {
            String[] cells = line.split(",");

            return KV.of(cells[0], line.length());
        }
    }));
    ```

1. `GroupByKey`: Group by the first column of the input.
    ```java
    PCollection<KV<String, Iterable<Integer>>> groupByKey = mapped.apply(GroupByKey.<String, Integer>create());
    ```
1. Convert the `PCollection<KV<String, Iterable<Integer>>>` into `PCollection<String>` just to write to output file.
    ```java
    PCollection<String> count = groupByKey.apply(ParDo.of(new ConvertKVToStringFn()));
    ```

    `ConvertKVToStringFn`:
    ```java
    /**
     * KV<String, Iterable<Integer> -> String
     */
    static class ConvertKVToStringFn extends DoFn<KV<String, Iterable<Integer>>, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output(String.valueOf(c.element()));
        }
    }
    ```

1. Write to output file.

    Input file is changed to have multiple keys:

    ```
    cat app/output-aggregated-0000*
    KV{BTC/JPY, [50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50]}
    KV{wronglyformatedrecord, [21]}
    KV{ETH/JPY, [50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50]}
    ```
