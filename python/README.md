# Apache Beam Training (Python)

## Prerequisite

- poetry: 1.3.2
- PubSub Topic
- PubSub Subscription
- GCS bucket

## Run

```
poetry run python -m streaming_wordcount --input_subscription projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID} --output_topic projects/${PROJECT_ID}/topics/${TOPIC_ID}-output --runner DataflowRunner --temp_location gs://${BUCKET_NAME}/temp/ --staging_location gs://${BUCKET_NAME}/staging/ --project $PROJECT_ID --region asia-northeast1
```

## References
1. https://cloud.google.com/pubsub/docs/emulator
