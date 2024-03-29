# Apache Beam Training

1. [Java](java)
1. [Python](python)

## Examples

Java:
1. [First Apache Beam Application](docs/java/01-first-apache-beam-application.md)
1. [ParDo and DoFn: Parallel Processing](docs/java/02-pardo-and-dofn-parallel-processing.md)
1. [KV + GroupByKey: Aggregation](docs/java/03-kv-groupbykey-aggregation.md)
1. [MapElement.via(new SimpleFunction) <-> ParDo + DoFn](docs/java/04-mapelement-simplefunction-pardo-dofn.md)
1. [KV with Custom Class and GroupIntoBatches](docs/java/05-kv-with-custom-class-and-groupintobatches.md)
1. [MultiOutput: Failure Handling](docs/java/06-multioutput-failure-handling.md)
1. [MultiOutput: with differnt types](docs/java/07-multioutput-with-different-types.md)
1. [Read from Google PubSub](docs/java/08-read-from-pubsub.md)

Python:
1. [Read and write PubSub](docs/python/01-read-and-write-pubsub.md)
1. [Read and write PubSub proto message](docs/python/02-read-and-write-pubsub-proto-message.md)
1. [Read and write PubSub with deduplication](https://cloud.google.com/blog/products/data-analytics/handling-duplicate-data-in-streaming-pipeline-using-pubsub-dataflow) (ToDo)

## References

1. Study Resource:
    1. https://www.youtube.com/c/ApacheBeamYT/videos
    1. [Streaming Engine: Execution Model for Highly-Scalable, Low-Latency Data Processing](https://medium.com/google-cloud/streaming-engine-execution-model-1eb2eef69a8e)
1. Error Handling:
    1. https://medium.com/@vallerylancey/error-handling-elements-in-apache-beam-pipelines-fffdea91af2a
    1. https://www.linuxdeveloper.space/retry-apache-beam-flink/
    1. https://medium.com/@bravnic/apache-beam-fundamentals-765ea5b59565
    1. https://stackoverflow.com/questions/53392311/apache-beam-retrytransienterrors-neverretry-does-not-respect-table-not-found-err
1. Examples:
    1. https://github.com/apache/beam/tree/master/examples/java
    1. https://medium.com/google-cloud/bigtable-beam-dataflow-cryptocurrencies-gcp-terraform-java-maven-4e7873811e86
    1. https://github.com/GoogleCloudPlatform/DataflowTemplates/tree/main/v2/googlecloud-to-elasticsearch/docs/PubSubToElasticsearch
1. IO:
    1. https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/introduction.html
    1. https://github.com/GoogleCloudPlatform/DataflowTemplates/blob/main/v2/elasticsearch-common/src/main/java/com/google/cloud/teleport/v2/elasticsearch/utils/ElasticsearchIO.java#L1100
1. Coder
    1. https://github.com/apache/beam/pull/663
    1. https://github.com/GoogleCloudPlatform/DataflowJavaSDK/issues/298
    1. https://timbrowndatablog.medium.com/apache-beam-coder-performance-4415cd0a1030
    1. https://stackoverflow.com/questions/28032063/how-to-fix-dataflow-unable-to-serialize-my-dofn
1. KafkaIO+Protobuf: https://selectfrom.dev/apache-beam-python-dataflow-kafkaio-for-protobuf-message-streaming-f349119850ad
