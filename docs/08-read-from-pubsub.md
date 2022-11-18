# Read from PubSub

## Prepare PubSub

1. login

    ```
    gcloud auth login
    gcloud auth application-default login
    ```

1. set project

    ```
    PROJECT_ID=masatonaka1989
    gcloud config set project $PROJECT_ID
    ```

1. Create PubSub

    ```
    gcloud pubsub topics create apache-beam-traing --project $PROJECT_ID
    Created topic [projects/masatonaka1989/topics/apache-beam-traing].
    ```

1. Create PubSub subscription

    ```
    gcloud pubsub subscriptions create apache-beam-training --topic projects/masatonaka1989/topics/apache-beam-traing --project $PROJECT_ID
    Created subscription [projects/masatonaka1989/subscriptions/apache-beam-training].
    ```


1. GCS `naka-apache-beam-training`

    ```
    gcloud storage buckets create gs://naka-apache-beam-training --project $PROJECT_ID
    Creating gs://naka-apache-beam-training/...
    ```

    ```
    gsutil ls -p $PROJECT_ID gs://
    gs://naka-apache-beam-training/
    ```

## Steps

1. Add the following code to `app/build.gradle.kts`


    Necessary for `PubSub`:
    ```kts
    // https://cloud.google.com/pubsub/docs/publish-receive-messages-client-library#install
    implementation("org.apache.beam:beam-runners-google-cloud-dataflow-java:2.13.0")
    ```

    Necessary to use `org.apache.beam.examples.common.WriteOneFilePerWindow`:
    ```kts
    // https://mvnrepository.com/artifact/org.apache.beam/beam-examples-java
    implementation("org.apache.beam:beam-examples-java:2.41.0")
    ```

1. Read from PubSub

    ```java
    // Input: read from PubSub
    PCollection<String> textData = p.apply("Read PubSub Message", PubsubIO.readStrings()
        .fromSubscription("projects/masatonaka1989/subscriptions/apache-beam-training"));
    ```

1. Change `GroupBy`.
    ```
    GroupByKey cannot be applied to non-bounded PCollection in the GlobalWindow without a trigger. Use a Window.into or Window.triggering transform prior to GroupByKey.
    ```

    Ref: [Window basics](https://beam.apache.org/documentation/programming-guide/#windowing-basics)

    ```java
    PCollection<String> textData =
        p.apply("Read PubSub Message",
            PubsubIO.readStrings().fromSubscription("projects/masatonaka1989/subscriptions/apache-beam-traing"))
            .apply(Window.<String>into(SlidingWindows.of(Duration.standardSeconds(30))
                .every(Duration.standardSeconds(5))));
    ```
1. Change `Must use windowed writes when applying WriteFiles to an unbounded PCollection`

    ```
    ```

1. Change `TextIO` to support unbounded PCollection.

    ```
    ```

1. Run
    ```
    ./gradlew build
    ```

1. Publish message to PubSub

    ```
    gcloud pubsub topics publish apache-beam-traing --message="BTC/JPY,bitflyer,1519845731987,1127174.0,1126166.0"
    gcloud pubsub topics publish apache-beam-traing --message="ETH/JPY,bitflyer,1519845742363,1127470.0,1126176.0"
    gcloud pubsub topics publish apache-beam-traing --message="BTC/JPY,bitflyer,1519845752427,1127601.0,1126227.0"
    ```

## References

1. https://chengzhizhao.medium.com/reading-apache-beam-programming-guide-3-pcollections-with-marvel-battle-stream-producer-2df5e6bbcabf
