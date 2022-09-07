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
