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
  public static final TupleTag<Failure> failuresTag = new TupleTag<Failure>() {};


  public static PCollectionTuple process(PCollection<KV<String, Iterable<CryptoCurrency>>> batch) {
    return batch.apply("Create PubSub objects",
        ParDo.of(new DoFn<KV<String, Iterable<CryptoCurrency>>, String>() {
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
              // c.output(batchResult.toString());
              out.get(validTag).output(batchResult.toString());

            } catch (Throwable throwable) {
              Failure failure = new Failure(batch, batch, throwable);
              // c.output(failuresTag, failure);
              out.get(failuresTag).output(failure);
            }

          }
        }).withOutputTags(validTag, TupleTagList.of(failuresTag)));
  }
}
