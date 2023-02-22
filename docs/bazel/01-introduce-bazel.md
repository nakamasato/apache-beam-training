# Introduce Bazel for building proto

1. WORKSPACE

    ```
    load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

    ## Protobuf
    http_archive(
        name = "com_google_protobuf",
        sha256 = "3bd7828aa5af4b13b99c191e8b1e884ebfa9ad371b0ce264605d347f135d2568",
        strip_prefix = "protobuf-3.19.4",
        urls = ["https://github.com/protocolbuffers/protobuf/archive/refs/tags/v3.19.4.tar.gz"],
    )

    load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

    protobuf_deps()

    ## Poetry
    http_archive(
        name = "com_sonia_rules_poetry",
        sha256 = "6dcb6ee86a9d507ef356097c5f2e16cb5e01c32021ff13cd28c0bb17bf5d8266",
        strip_prefix = "rules_poetry-d7a852ae69d22fe4670e34822cd376a69db0485e",
        urls = ["https://github.com/soniaai/rules_poetry/archive/d7a852ae69d22fe4670e34822cd376a69db0485e.tar.gz"],
    )

    load("@com_sonia_rules_poetry//rules_poetry:defs.bzl", "poetry_deps")

    poetry_deps()

    load("@com_sonia_rules_poetry//rules_poetry:poetry.bzl", "poetry")

    poetry(
        name = "poetry",
        lockfile = "//python:poetry.lock",
        pyproject = "//python:pyproject.toml",
    )

    ## gRPC
    http_archive(
        name = "com_github_grpc_grpc",
        sha256 = "ec125d7fdb77ecc25b01050a0d5d32616594834d3fe163b016768e2ae42a2df6",
        strip_prefix = "grpc-1.52.1",
        urls = [
            "https://github.com/grpc/grpc/archive/refs/tags/v1.52.1.tar.gz",
        ],
    )

    load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", "grpc_deps")

    grpc_deps()
    ```

1. Add `proto/wordcount/BUILD.bazel` (for python code generation)
    ```
    load("@rules_proto//proto:defs.bzl", "proto_library")
    load("@com_github_grpc_grpc//bazel:python_rules.bzl", "py_proto_library")

    proto_library(
        name = "proto",
        strip_import_prefix = "/proto",
        srcs = [
            "wordcount.proto",
        ],
        visibility = ["//visibility:public"],
    )

    py_proto_library(
        name = "py_proto",
        deps = [
            ":proto",
        ],
        visibility = ["//visibility:public"],
    )
    ```

    with `strip_import_prefix = "/proto",`, you can import `from wordcount.wordcount_pb2 import WordCount`

1. Update `python/BUILD.bazel`

    ```
    load("@rules_python//python:defs.bzl", "py_binary")
    load("@poetry//:dependencies.bzl", "dependency")
    load("@com_google_protobuf//:protobuf.bzl", "py_proto_library")

    py_binary(
        name = "streaming_wordcount_proto",
        srcs = ["streaming_wordcount_proto.py"],
        deps = [
            dependency("pandas"),
            "//proto:py_proto",
        ],
    )
    ```
1. Run
    ```
    bazel run //python:streaming_wordcount_proto -- --runner directrunner
    ```
