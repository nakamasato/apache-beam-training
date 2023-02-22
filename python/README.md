# Apache Beam Training (Python)

## Prerequisite

- poetry: 1.3.2 (1.22.0 with Bazel as the new lock format is not supported [ref](https://github.com/soniaai/rules_poetry/pull/23))
    ```
    curl -sSL https://install.python-poetry.org | python3 - --uninstall
    curl -sSL https://install.python-poetry.org | python3 - --version 1.2.2
    ```
- PubSub Topic
- PubSub Subscription
- GCS bucket

## Run


1. streaming_wordcount on dataflow

    ```
    poetry run python -m streaming_wordcount --input_subscription projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID} --output_topic projects/${PROJECT_ID}/topics/${TOPIC_ID}-output --runner DataflowRunner --temp_location gs://${BUCKET_NAME}/temp/ --staging_location gs://${BUCKET_NAME}/staging/ --project $PROJECT_ID --region asia-northeast1
    ```
1. streaming_wordcount_proto in local
    ```
    poetry run python streaming_wordcount_proto.py --runner directrunner
    Traceback (most recent call last):
      File "/Users/m.naka/repos/nakamasato/apache-beam-training/python/streaming_wordcount_proto.py", line 10, in <module>
        from proto.wordcount_pb2 import WordCount
    ModuleNotFoundError: No module named 'proto.wordcount_pb2'
    ```

    `proto.wordcount_pb2` is necessary to use Bazel but it's not found if you run with Python.

## References
1. https://cloud.google.com/pubsub/docs/emulator
