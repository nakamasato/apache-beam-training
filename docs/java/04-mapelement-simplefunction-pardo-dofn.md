# [4. MapElement.via(new SimpleFunction) <-> ParDo + DoFn](https://github.com/nakamasato/apache-beam-training/tree/e87b4a530832b42f9c850eab06a527591e9395e7)

## 4.1. ConvertStringIntoKVFn

`MapElement.via(new SimpleFunction)`:

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

`ParDo` + `DoFn`:

```java
    /*
     * String -> KV<String, Integer>
     */
    static class ConvertStringIntoKVFn extends DoFn<String, KV<String, Integer>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String row = c.element();
            String[] cells = row.split(",");
            c.output(KV.of(cells[0], row.length()));
        }
    }
```

and

```java
PCollection<KV<String, Integer>> mapped = textData.apply(ParDo.of(new ConvertStringIntoKVFn()));
```

## 4.2. ConvertKVToStringFn

`SimpleFunction`:

```java
PCollection<String> count = groupByKey.apply(MapElements.via(new SimpleFunction<KV<String, Iterable<Integer>>, String>() {
            @Override
            public String apply(KV<String, Iterable<Integer>> kv) {
                return String.valueOf(kv);
            }
        }));
```

`ConvertKVToStringFn` (ParDo + DoFn):

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
