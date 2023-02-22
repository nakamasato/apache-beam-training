# [Write PubSub proto message (DataFlow)](https://cloud.google.com/pubsub/docs/samples/pubsub-publish-proto-messages#pubsub_publish_proto_messages-python)


## Prerequisite

Install `protoc` [3.19.4](https://github.com/protocolbuffers/protobuf/releases/tag/v3.19.4)

```
VERSION=3.19.4
PROTOC_ZIP=protoc-$VERSION-osx-x86_64.zip
curl -OL https://github.com/protocolbuffers/protobuf/releases/download/v$VERSION/$PROTOC_ZIP
unzip -o $PROTOC_ZIP -d $HOME/.local
rm -f $PROTOC_ZIP
```

```
protoc --version
libprotoc 3.19.4
```

Ref: https://grpc.io/docs/protoc-installation/#install-pre-compiled-binaries-any-os

**The version of `protoc` must be same as the version of `protobuf`**

## 1. Write Proto message to text file
1. Add `protobuf`
    ```
    poetry add protobuf=3.19.4
    ```

    > Because apache-beam (2.45.0) depends on protobuf (>3.12.2,<3.19.5)

1. Proto
    ```protobuf
    syntax = "proto3";
    package wordcount;

    message WordCount {
      string word = 1;
      uint64 count = 2;
    }
    ```
1. Compile Proto (run in the root dir)
    ```
    protoc -I=python --proto_path=proto/wordcount --python_out=python proto/wordcount/wordcount.proto
    ```

    -> `wordcount_pb2.py` will be generated.

1. Create a new file `streaming_wordcount_proto.py`

1. Run
    ```
    poetry run python streaming_wordcount_proto.py --runner directrunner
    ```

    <details>

    ```
    INFO:root:Default Python SDK image for environment is apache/beam_python3.9_sdk:2.45.0
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function annotate_downstream_side_inputs at 0x126e06430> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function fix_side_input_pcoll_coders at 0x126e06550> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function pack_combiners at 0x126e06a60> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function lift_combiners at 0x126e06af0> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function expand_sdf at 0x126e06ca0> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function expand_gbk at 0x126e06d30> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function sink_flattens at 0x126e06e50> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function greedily_fuse at 0x126e06ee0> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function read_to_impulse at 0x126e06f70> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function impulse_to_input at 0x126e07040> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function sort_stages at 0x126e07280> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function add_impulse_to_dangling_transforms at 0x126e073a0> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function setup_timer_mapping at 0x126e071f0> ====================
    INFO:apache_beam.runners.portability.fn_api_runner.translations:==================== <function populate_data_channel_coders at 0x126e07310> ====================
    INFO:apache_beam.runners.worker.statecache:Creating state cache with size 104857600
    INFO:apache_beam.runners.portability.fn_api_runner.worker_handlers:Created Worker handler <apache_beam.runners.portability.fn_api_runner.worker_handlers.EmbeddedWorkerHandler object at 0x126f291f0> for environment ref_Environment_default_environment_1 (beam:env:embedded_python:v1, b'')
    INFO:apache_beam.io.filebasedsink:Starting finalize_write threads with num_shards: 1 (skipped: 0), batches: 1, num_threads: 1
    INFO:apache_beam.io.filebasedsink:Renamed 1 shards in 0.01 seconds.
    ```

    </details>

1. Check result

    <details><summary>output/wordcount--00000-of-00001</summary>

    ```
    cat output/wordcount--00000-of-00001
    word: "This"
    count: 1

    word: "is"
    count: 1

    word: "a"
    count: 1

    word: "test"
    count: 1

    word: "message"
    count: 2

    word: "Another"
    count: 1

    word: "also"
    count: 1

    word: "has"
    count: 1

    word: "some"
    count: 1

    word: "text"
    count: 1
    ```

    </details>

## 2. Run with bazel

1. Ensure your poetry version is `<v1.3.0`
    ```
    poetry --version
    Poetry (version 1.2.2)
    ```
1. Add pandas
    ```
    poetry add pandas==1.2.3
    ```
1. Run with bazel
    ```
    bazel run //python:streaming_wordcount_proto -- --runner directrunner
    ```
## 2. Write Proto message from Dataflow

