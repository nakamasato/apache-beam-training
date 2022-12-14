/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package apachebeamtraining;

import java.util.Date;
import java.util.Iterator;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.GroupIntoBatches;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static Logger logger = LoggerFactory.getLogger(App.class);

  /*
   * Extract 5th field of the input text.
   */
  public static class ExtractAmountFromRowFn extends DoFn<String, String> {
    @ProcessElement
    public void process(ProcessContext c) {

      try {
        String row = c.element();

        String[] cells = row.split(",");

        c.output(cells[4]);
      } catch (java.lang.ArrayIndexOutOfBoundsException e) {
        logger.error("Failed to process data", e);
      }
    }
  }

  /**
   * T -> String
   */
  static class ConvertToStringFn<T> extends DoFn<T, String> {
    @ProcessElement
    public void processElement(@Element T e, OutputReceiver<String> receiver) {
      receiver.output(e.toString());
    }
  }

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

  /*
   * String -> KV<String, CryptoCurrency>
   */
  static class ConvertTextIntoKVCryptoCurrencyFn extends DoFn<String, KV<String, CryptoCurrency>> {
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

  /*
   * Process item in a batch. Just count the number of item and return BatchResult as String.
   */
  static class ProcessBatch extends DoFn<KV<String, Iterable<CryptoCurrency>>, String> {
    @ProcessElement
    public void process(@Element KV<String, Iterable<CryptoCurrency>> batch,
        OutputReceiver<String> receiver) {
      int count = 0;
      Iterator<CryptoCurrency> iter = batch.getValue().iterator();
      while (iter.hasNext()) {
        count++;
        iter.next();
      }
      BatchResult batchResult =
          new BatchResult(batch.getKey(), count, new Date(System.currentTimeMillis()));
      receiver.output(batchResult.toString());
    }
  }

  public static void main(String[] args) {

    PipelineOptions options = PipelineOptionsFactory.create();

    Pipeline p = Pipeline.create(options);

    // Input: read from file
    PCollection<String> textData = p.apply(TextIO.read().from("input-record.txt"));

    // Process1: Group by the cryptocurrency name and write each line length to the file.
    PCollection<KV<String, Integer>> mapped = textData.apply(ParDo.of(new ConvertStringIntoKVFn()));
    PCollection<KV<String, Iterable<Integer>>> groupByKey =
        mapped.apply(GroupByKey.<String, Integer>create());
    PCollection<String> count =
        groupByKey.apply(ParDo.of(new ConvertToStringFn<KV<String, Iterable<Integer>>>()));
    count.apply(TextIO.write().to("output-aggregated"));

    // Process2: Extract the amount of each line and write it to the file.
    PCollection<String> bidData = textData.apply(ParDo.of(new ExtractAmountFromRowFn()));
    bidData.apply(TextIO.write().to("output-map"));

    // Process3: GroupIntoBatches
    PCollection<KV<String, CryptoCurrency>> cryptoKV =
        textData.apply(ParDo.of(new ConvertTextIntoKVCryptoCurrencyFn()));
    PCollection<KV<String, Iterable<CryptoCurrency>>> batchedCrypt =
        cryptoKV.apply(GroupIntoBatches.<String, CryptoCurrency>ofSize(3));
    PCollection<String> output = batchedCrypt.apply(ParDo.of(new ProcessBatch()));
    output.apply(TextIO.write().to("output-count"));

    // Process4: Use MultipleOutput
    PCollectionTuple outputTuple = MultipleOutput.process(batchedCrypt);
    PCollection<BatchResult> success = outputTuple.get(MultipleOutput.validTag);
    PCollection<Failure> failure = outputTuple.get(MultipleOutput.failureTag);
    success.apply(ParDo.of(new ConvertToStringFn<BatchResult>()))
        .apply(TextIO.write().to("output-success"));
    failure.apply(ParDo.of(new ConvertToStringFn<Failure>()))
        .apply(TextIO.write().to("output-failure"));

    p.run().waitUntilFinish();
  }
}
