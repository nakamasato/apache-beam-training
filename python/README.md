# Apache Beam Training (Python)

## Overview
pubsub topic/subscription -> dataflow -> pubsub topic


## GCP preparation

enable dataflow & pubsub api
```
gcloud services enable dataflow.googleapis.com
gcloud services enable pubsub.googleapis.com
```

set env var
```
PROJECT_ID=<your project>
TOPIC_ID=apache-beam-training
SUBSCRIPTION_ID=apache-beam-training
BUCKET_NAME=naka-apache-beam-training
```

```
gcloud config set project $PROJECT_ID
```

```
gcloud pubsub topics create ${TOPIC_ID} --project $PROJECT_ID
```

```
gcloud pubsub topics create ${TOPIC_ID}-output --project $PROJECT_ID
```

```
gcloud pubsub subscriptions create $SUBSCRIPTION_ID --topic projects/${PROJECT_ID}/topics/${TOPIC_ID} --project $PROJECT_ID
```

```
gcloud pubsub subscriptions create ${SUBSCRIPTION_ID}-output --topic projects/${PROJECT_ID}/topics/${TOPIC_ID}-output --project $PROJECT_ID
```
```
gcloud storage buckets create gs://${BUCKET_NAME} --project $PROJECT_ID
```


## Development

1. Python
    ```
    python -V
    Python 3.9.9
    ```
1. `poetry init`
1. Update pyproject.toml: `packages = []`
1. `poetry add wheel 'apache-beam[gcp]'`
1. `curl -O https://raw.githubusercontent.com/apache/beam/master/sdks/python/apache_beam/examples/streaming_wordcount.py`
1. `--input_subscription`
1. set default auth
    ```
    gcloud auth application-default login
    ```
1. Run
    ```
    poetry run python -m streaming_wordcount --input_subscription projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID} --output_topic projects/${PROJECT_ID}/topics/${TOPIC_ID}-output --runner DataflowRunner --temp_location gs://${BUCKET_NAME}/temp/ --staging_location gs://${BUCKET_NAME}/staging/ --project $PROJECT_ID --region asia-northeast1
    ```

    <details>

    ```
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:52.913Z: JOB_MESSAGE_BASIC: Running job using Streaming Engine
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:52.939Z: JOB_MESSAGE_DEBUG: Workflow config is missing a default resource spec.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:52.966Z: JOB_MESSAGE_DEBUG: Adding StepResource setup and teardown to workflow graph.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:52.996Z: JOB_MESSAGE_DEBUG: Adding workflow start and stop steps.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:53.028Z: JOB_MESSAGE_DEBUG: Assigning stage ids.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:54.092Z: JOB_MESSAGE_DEBUG: Starting worker pool setup.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:54.124Z: JOB_MESSAGE_DEBUG: Starting worker pool setup.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:50:54.155Z: JOB_MESSAGE_BASIC: Starting 1 workers in asia-northeast1-a...
    INFO:apache_beam.runners.dataflow.dataflow_runner:Job 2023-02-16_16_50_40-8403974779991517351 is in state JOB_STATE_RUNNING
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:51:58.476Z: JOB_MESSAGE_DETAILED: Autoscaling: Raised the number of workers to 1 so that the pipeline can catch up with its backlog and keep up with its input rate.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:52:30.514Z: JOB_MESSAGE_DETAILED: Workers have started successfully.
    INFO:apache_beam.runners.dataflow.dataflow_runner:2023-02-17T00:52:45.310Z: JOB_MESSAGE_DETAILED: All workers have finished the startup processes and began to receive work requests.
    ```

    </details>

    Check status

    ```
    ```

1. Pubish message to the input topic

    ```
    gcloud pubsub topics publish apache-beam-training --message="To see how a pipeline runs locally, use a ready-made Python module for the wordcount example that is included with the apache_beam package."
    ```

1. Check message from output topic
    ```
    gcloud pubsub subscriptions pull ${SUBSCRIPTION_ID}-output --auto-ack
    ```

    ```
    python git:(python) ✗ gcloud pubsub subscriptions pull ${SUBSCRIPTION_ID}-output --auto-ack
    ┌─────────┬──────────────────┬──────────────┬────────────┬──────────────────┐
    │   DATA  │    MESSAGE_ID    │ ORDERING_KEY │ ATTRIBUTES │ DELIVERY_ATTEMPT │
    ├─────────┼──────────────────┼──────────────┼────────────┼──────────────────┤
    │ that: 1 │ 6996067700100118 │              │            │                  │
    └─────────┴──────────────────┴──────────────┴────────────┴──────────────────┘
    python git:(python) ✗ gcloud pubsub subscriptions pull ${SUBSCRIPTION_ID}-output --auto-ack
    ┌────────┬──────────────────┬──────────────┬────────────┬──────────────────┐
    │  DATA  │    MESSAGE_ID    │ ORDERING_KEY │ ATTRIBUTES │ DELIVERY_ATTEMPT │
    ├────────┼──────────────────┼──────────────┼────────────┼──────────────────┤
    │ for: 1 │ 6996082509976500 │              │            │                  │
    └────────┴──────────────────┴──────────────┴────────────┴──────────────────┘
    python git:(python) ✗ gcloud pubsub subscriptions pull ${SUBSCRIPTION_ID}-output --auto-ack
    ┌──────┬──────────────────┬──────────────┬────────────┬──────────────────┐
    │ DATA │    MESSAGE_ID    │ ORDERING_KEY │ ATTRIBUTES │ DELIVERY_ATTEMPT │
    ├──────┼──────────────────┼──────────────┼────────────┼──────────────────┤
    │ a: 2 │ 6996066240654555 │              │            │                  │
    └──────┴──────────────────┴──────────────┴────────────┴──────────────────┘
    ```
1. Stop the job

    Check job id:

    ```
    gcloud dataflow jobs list
    ```

    Cancel the job:

    ```
    gcloud dataflow jobs cancel <JOB_ID> --region asia-northeast1
    ```
## Cleanup

Delete resources

```
gcloud pubsub subscriptions delete $SUBSCRIPTION_ID  --project $PROJECT_ID
gcloud pubsub subscriptions delete ${SUBSCRIPTION_ID}-output --project $PROJECT_ID
gcloud pubsub topics delete ${TOPIC_ID} --project $PROJECT_ID
gcloud pubsub topics delete ${TOPIC_ID}-output --project $PROJECT_ID
gsutil rm -rf "gs://${BUCKET_NAME}/temp/*"
gsutil rm -rf "gs://${BUCKET_NAME}/staging/*"
gcloud storage buckets delete gs://${BUCKET_NAME} --project $PROJECT_ID
```

Revoke local credentials
```
gcloud auth application-default revoke
gcloud auth revoke
gcloud config unset project
```

## Errors

1. `Workflow failed. Causes: Network default is not accessible to Dataflow Service account or does not exist. For network configuration instructions, see https://cloud.google.com/dataflow/docs/guides/specifying-networks.`
    1. https://stackoverflow.com/questions/51362560/network-default-is-not-accessible-to-dataflow-service-account
    1. You can create a default VPC network with auto mode ([ref](https://cloud.google.com/vpc/docs/vpc#auto-mode-considerations))
