# [8. Read from PubSub](https://github.com/nakamasato/apache-beam-training/tree/4e78dae16eacc9930ef44af340681b4c97e783cb)

## Prepare GCP resources (PubSub, Storage, Service Account)

1. `gcloud` login

    ```
    gcloud auth login
    gcloud auth application-default login
    ```

1. Set project

    ```
    PROJECT_ID=masatonaka1989
    gcloud config set project $PROJECT_ID
    gcloud config list
    [core]
    account = masatonaka1989@gmail.com
    disable_usage_reporting = False
    project = masatonaka1989

    Your active configuration is: [default]
    ```

1. Create PubSub

    ```
    TOPIC_ID=apache-beam-training
    gcloud pubsub topics create ${TOPIC_ID} --project $PROJECT_ID
    ```

    ```
    gcloud pubsub topics list --filter 'name:projects/masatonaka1989/topics/apache-beam-training'
    ---
    name: projects/masatonaka1989/topics/apache-beam-training
    ```

1. Create PubSub subscription

    ```
    SUBSCRIPTION_ID=apache-beam-training
    gcloud pubsub subscriptions create $SUBSCRIPTION_ID --topic projects/${PROJECT_ID}/topics/${TOPIC_ID} --project $PROJECT_ID
    Created subscription [projects/masatonaka1989/subscriptions/apache-beam-training].
    ```

1. GCS `naka-apache-beam-training`

    ```
    BUCKET_NAME=naka-apache-beam-training
    gcloud storage buckets create gs://${BUCKET_NAME} --project $PROJECT_ID
    Creating gs://naka-apache-beam-training/...
    ```

    ```
    gcloud storage buckets list --filter "name:${BUCKET_NAME}"
    ```

    or

    ```
    gsutil ls -p $PROJECT_ID gs://
    gs://${BUCKET_NAME}/
    ```
1. Service Account and grant `roles/dataflow.worker`, `roles/storage.objectAdmin`, `roles/pubsub.admin`.
    ```
    SERVICE_ACCOUNT=apache-beam-training
    gcloud iam service-accounts create $SERVICE_ACCOUNT --project $PROJECT_ID
    gcloud projects add-iam-policy-binding $PROJECT_ID --member="serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com" --role=roles/dataflow.worker
    gcloud projects add-iam-policy-binding $PROJECT_ID --member="serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com" --role=roles/storage.objectAdmin
    gcloud projects add-iam-policy-binding $PROJECT_ID --member="serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com" --role=roles/pubsub.admin
    ```
## Implementation

1. Add the following code to `app/build.gradle.kts`

    Necessary for `PubSub`:
    ```kts
    // https://cloud.google.com/pubsub/docs/publish-receive-messages-client-library#install
    implementation("org.apache.beam:beam-runners-google-cloud-dataflow-java:2.43.0")
    ```

    Necessary to use `org.apache.beam.examples.common.WriteOneFilePerWindow`:
    ```kts
    // https://mvnrepository.com/artifact/org.apache.beam/beam-examples-java
    implementation("org.apache.beam:beam-examples-java:2.43.0")
    ```

1. Create `PubSubToGcsOptions` interface (from [example](https://cloud.google.com/pubsub/docs/stream-messages-dataflow)). This enables you to provide subscription name, window size, and output.

    ```java
    public interface PubSubToGcsOptions extends PipelineOptions, StreamingOptions {
      @Description("The Cloud Pub/Sub subscription to read from.")
      @Required
      String getInputSubscription();

      void setInputSubscription(String value);

      @Description("Output file's window size in number of minutes.")
      @Default.Integer(1)
      Integer getWindowSize();

      void setWindowSize(Integer value);

      @Description("Path of the output file including its filename prefix.")
      @Required
      String getOutput();

      void setOutput(String value);
    }
    ```

1. Create a PipelineOption in `main`

    ```java
    PubSubToGcsOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);

    options.setStreaming(true);

    Pipeline p = Pipeline.create(options);
    ```

1. Read from PubSub.
    ```java
    PCollection<String> textData = p.apply("Read PubSub Message",
        PubsubIO.readStrings().fromSubscription(options.getInputSubscription()));
    ```
1. Make a window.
    ```java
    PCollection<String> windowedTextData =
        textData.apply(Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize()))));
    ```

    GroupByKey cannot be applied to non-bounded PCollection in the GlobalWindow without a trigger. Use a Window.into or Window.triggering transform prior to GroupByKey.

    Ref: [Window basics](https://beam.apache.org/documentation/programming-guide/#windowing-basics)

    `Must use windowed writes when applying WriteFiles to an unbounded PCollection`

1. Write to GCS.

    You can also use the following code (ref: [Writing windowed or unbounded data](https://beam.apache.org/releases/javadoc/2.2.0/org/apache/beam/sdk/io/TextIO.html))
    ```java
    windowedTextData
        .apply(TextIO.write().to(options.getOutput()).withWindowedWrites().withNumShards(1));
    ```

<details>

```java
  public static void main(String[] args) {

    // For local mode, you do not need to set the runner since DirectRunner is already the default.
    PubSubToGcsOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);

    options.setStreaming(true);

    Pipeline p = Pipeline.create(options);

    // Input: read from PubSub
    PCollection<String> textData = p.apply("Read PubSub Message",
        PubsubIO.readStrings().fromSubscription(options.getInputSubscription()));

    // Make a Window
    PCollection<String> windowedTextData =
        textData.apply(Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize()))));

    // Write to file
    windowedTextData.apply("Write Files to GCS", new WriteOneFilePerWindow(options.getOutput(), 1));

    p.run().waitUntilFinish();
  }
```

</details><summary>main()</summary>

## Run in local

1. Run in local (with DirectRunner) (ToDo)
    ```
    ./gradlew run --args="--inputSubscription=projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID} --output=gs://${BUCKET_NAME}/samples/output --gcpTempLocation=gs://${BUCKET_NAME}/temp"
    ```

1. Check GCS
    ```
    gsutil ls -p $PROJECT_ID gs://naka-apache-beam-training/
    gs://naka-apache-beam-training/temp/
    ```

    No result file is generated. why?

## Run on Dataflow

1. Run on dataflow with service account `apache-beam-training`
    ```
    ./gradlew run --args="--inputSubscription=projects/${PROJECT_ID}/subscriptions/${SUBSCRIPTION_ID} --output=gs://${BUCKET_NAME}/samples/output --gcpTempLocation=gs://${BUCKET_NAME}/temp --runner=DataflowRunner --serviceAccount=${SERVICE_ACCOUNT}@${PROJECT_ID}.iam.gserviceaccount.com --project=${PROJECT_ID} --region=us-central1"
    ```

1. Publish message to PubSub

    ```
    gcloud pubsub topics publish apache-beam-training --message="BTC/JPY,bitflyer,1519845731987,1127174.0,1126166.0"
    gcloud pubsub topics publish apache-beam-training --message="ETH/JPY,bitflyer,1519845742363,1127470.0,1126176.0"
    gcloud pubsub topics publish apache-beam-training --message="BTC/JPY,bitflyer,1519845752427,1127601.0,1126227.0"
    ```

1. Check the job

    ```
    gcloud dataflow jobs list --project $PROJECT_ID --filter "type:streaming AND state:running" --region us-central1
    JOB_ID                                    NAME                            TYPE       CREATION_TIME        STATE    REGION
    2023-02-11_00_45_08-17780620775586673870  app-m0naka-0211084502-e7a39d6e  Streaming  2023-02-11 08:45:09  Running  us-central1
    ```

1. Check GCS contents.

    ```
    gsutil cat -h gs://naka-apache-beam-training/samples/output-08:44-08:44-0-of-1

    ==> gs://naka-apache-beam-training/samples/output-08:44-08:44-0-of-1 <==
    BTC/JPY,bitflyer,1519845731987,1127174.0,1126166.0
    ```

1. Cleanup

    1. Cancel the Job.
        ```
        gcloud dataflow jobs cancel 2023-02-11_00_45_08-17780620775586673870 --project $PROJECT_ID --region us-central1
        ```
    1. Delete contents in GCS
        ```
        gsutil -m rm -rf gs://${BUCKET_NAME}/samples/
        gsutil -m rm -rf "gs://${BUCKET_NAME}/temp/*"
        ```
    1. Delete Bucket
        ```
        gsutil rb gs://${BUCKET_NAME}
        ```
    1. Delete PubSub Subscription & Topic
        ```
        gcloud pubsub subscriptions delete $SUBSCRIPTION_ID
        gcloud pubsub topics delete $TOPIC_ID
        ```
    1. Delete Service Account
        ```
        gcloud iam service-accounts delete ${SERVICE_ACCOUNT}@${PROJECT_ID}.iam.gserviceaccount.com
        ```
    1. Revoke auth
        ```
        gcloud auth application-default revoke
        gcloud auth revoke
        gcloud config unset project
        ```
## Error

### Error1: `java.lang.ClassNotFoundException: org.apache.beam.runners.core.construction.PipelineResources`

<details>

```
Feb 11, 2023 8:28:24 AM com.google.auth.oauth2.DefaultCredentialsProvider warnAboutProblematicCredentials
WARNING: Your application has authenticated using end user credentials from Google Cloud SDK. We recommend that most server applications use service accounts instead. If your application continues to use end user credentials from Cloud SDK, you might receive a "quota exceeded" or "API not enabled" error. For more information about service accounts, see https://cloud.google.com/docs/authentication/.
Exception in thread "main" java.lang.RuntimeException: Encountered checked exception when constructing an instance from factory method DataflowRunner#fromOptions(interface org.apache.beam.sdk.options.PipelineOptions)
        at org.apache.beam.sdk.util.InstanceBuilder.buildFromMethod(InstanceBuilder.java:233)
        at org.apache.beam.sdk.util.InstanceBuilder.build(InstanceBuilder.java:158)
        at org.apache.beam.sdk.PipelineRunner.fromOptions(PipelineRunner.java:55)
        at org.apache.beam.sdk.Pipeline.create(Pipeline.java:155)
        at apachebeamtraining.App.main(App.java:150)
Caused by: java.lang.NoClassDefFoundError: org/apache/beam/runners/core/construction/PipelineResources
        at org.apache.beam.runners.dataflow.DataflowRunner.fromOptions(DataflowRunner.java:270)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
        at java.base/java.lang.reflect.Method.invoke(Method.java:578)
        at org.apache.beam.sdk.util.InstanceBuilder.buildFromMethod(InstanceBuilder.java:217)
        ... 4 more
Caused by: java.lang.ClassNotFoundException: org.apache.beam.runners.core.construction.PipelineResources
        at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
        at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521)
        ... 8 more
```

</details>

This was due to different versions specified in build.gradle.kts.

### Error2: `java.lang.IllegalStateException: You are currently running with version 2.0.0 of google-api-client. You need at least version 1.15 of google-api-client to run version 1.27.0 of the Dataflow API library.`

<details>

```
Exception in thread "main" java.lang.RuntimeException: Encountered checked exception when constructing an instance from factory method DataflowRunner#fromOptions(interface org.apache.beam.sdk.options.PipelineOptions)
        at org.apache.beam.sdk.util.InstanceBuilder.buildFromMethod(InstanceBuilder.java:233)
        at org.apache.beam.sdk.util.InstanceBuilder.build(InstanceBuilder.java:158)
        at org.apache.beam.sdk.PipelineRunner.fromOptions(PipelineRunner.java:55)
        at org.apache.beam.sdk.Pipeline.create(Pipeline.java:155)
        at apachebeamtraining.App.main(App.java:149)
Caused by: java.lang.ExceptionInInitializerError
        at java.base/java.lang.Class.forName0(Native Method)
        at java.base/java.lang.Class.forName(Class.java:315)
        at com.sun.proxy.$Proxy0.<clinit>(Unknown Source)
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:490)
        at org.apache.beam.sdk.util.InstanceBuilder.buildFromConstructor(InstanceBuilder.java:261)
        at org.apache.beam.sdk.util.InstanceBuilder.build(InstanceBuilder.java:160)
        at org.apache.beam.sdk.options.ProxyInvocationHandler.as(ProxyInvocationHandler.java:299)
        at org.apache.beam.sdk.options.ProxyInvocationHandler.invoke(ProxyInvocationHandler.java:199)
        at com.sun.proxy.$Proxy52.as(Unknown Source)
        at org.apache.beam.sdk.options.PipelineOptionsValidator.validate(PipelineOptionsValidator.java:76)
        at org.apache.beam.sdk.options.PipelineOptionsValidator.validate(PipelineOptionsValidator.java:50)
        at org.apache.beam.runners.dataflow.DataflowRunner.fromOptions(DataflowRunner.java:230)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.apache.beam.sdk.util.InstanceBuilder.buildFromMethod(InstanceBuilder.java:217)
        ... 4 more
Caused by: java.lang.IllegalStateException: You are currently running with version 2.0.0 of google-api-client. You need at least version 1.15 of google-api-client to run version 1.27.0 of the Dataflow API library.
        at com.google.common.base.Preconditions.checkState(Preconditions.java:534)
        at com.google.api.client.util.Preconditions.checkState(Preconditions.java:113)
        at com.google.api.services.dataflow.Dataflow.<clinit>(Dataflow.java:44)
        ... 24 more
```

https://github.com/googleapis/google-api-java-client-services/blob/f886a90300370b9e0d7d65f200739f942e705b34/clients/google-api-services-dataflow/v1b3/1.27.0/com/google/api/services/dataflow/Dataflow.java#L40-L50

```java
@SuppressWarnings("javadoc")
public class Dataflow extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.27.0 of the Dataflow API library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }
```

</details>

This was due to different versions specified in build.gradle.kts.

## Error3: Dataflow API is not enabled.

<details>

```
Caused by: com.google.api.client.googleapis.json.GoogleJsonResponseException: 400 Bad Request
POST https://dataflow.googleapis.com/v1b3/projects/masatonaka1989/locations/asia-northeast1/jobs
{
  "code" : 400,
  "errors" : [ {
    "domain" : "global",
    "message" : "(fa836c5b9e5ff8f8): Dataflow API is not enabled. Please use the Cloud Platform Console, https://console.developers.google.com/apis/api/dataflow.googleapis.com/overview?project=masatonaka1989, to enable Dataflow API.",
    "reason" : "failedPrecondition"
  } ],
  "message" : "(fa836c5b9e5ff8f8): Dataflow API is not enabled. Please use the Cloud Platform Console, https://console.developers.google.com/apis/api/dataflow.googleapis.com/overview?project=masatonaka1989, to enable Dataflow API.",
  "status" : "FAILED_PRECONDITION"
}
        at com.google.api.client.googleapis.json.GoogleJsonResponseException.from(GoogleJsonResponseException.java:146)
        at com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest.newExceptionOnError(AbstractGoogleJsonClientRequest.java:118)
        at com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest.newExceptionOnError(AbstractGoogleJsonClientRequest.java:37)
        at com.google.api.client.googleapis.services.AbstractGoogleClientRequest$1.interceptResponse(AbstractGoogleClientRequest.java:439)
        at com.google.api.client.http.HttpRequest.execute(HttpRequest.java:1111)
        at com.google.api.client.googleapis.services.AbstractGoogleClientRequest.executeUnparsed(AbstractGoogleClientRequest.java:525)
        at com.google.api.client.googleapis.services.AbstractGoogleClientRequest.executeUnparsed(AbstractGoogleClientRequest.java:466)
        at com.google.api.client.googleapis.services.AbstractGoogleClientRequest.execute(AbstractGoogleClientRequest.java:576)
        at org.apache.beam.runners.dataflow.DataflowClient.createJob(DataflowClient.java:64)
        at org.apache.beam.runners.dataflow.DataflowRunner.run(DataflowRunner.java:1381)
        ... 4 more
```

</details>

-> `gcloud services enable dataflow.googleapis.com`

## Error4: Current user cannot act as service account

<details>

```
Exception in thread "main" java.lang.RuntimeException: Failed to create a workflow job: (124268927654c5cb): Current user cannot act as service account apache-beam-training. Please grant your user account one of [Owner, Editor, Service Account Actor] roles, or any other role that includes the iam.serviceAccounts.actAs permission. See https://cloud.google.com/iam/docs/service-accounts-actas for additional details.
        at org.apache.beam.runners.dataflow.DataflowRunner.run(DataflowRunner.java:1395)
        at org.apache.beam.runners.dataflow.DataflowRunner.run(DataflowRunner.java:197)
        at org.apache.beam.sdk.Pipeline.run(Pipeline.java:323)
        at org.apache.beam.sdk.Pipeline.run(Pipeline.java:309)
        at apachebeamtraining.App.main(App.java:167)
Caused by: com.google.api.client.googleapis.json.GoogleJsonResponseException: 400 Bad Request
POST https://dataflow.googleapis.com/v1b3/projects/masatonaka1989/locations/asia-northeast1/jobs
{
  "code" : 400,
  "errors" : [ {
    "domain" : "global",
    "message" : "(124268927654c5cb): Current user cannot act as service account apache-beam-training. Please grant your user account one of [Owner, Editor, Service Account Actor] roles, or any other role that includes the iam.serviceAccounts.actAs permission. See https://cloud.google.com/iam/docs/service-accounts-actas for additional details.",
    "reason" : "badRequest"
  } ],
  "message" : "(124268927654c5cb): Current user cannot act as service account apache-beam-training. Please grant your user account one of [Owner, Editor, Service Account Actor] roles, or any other role that includes the iam.serviceAccounts.actAs permission. See https://cloud.google.com/iam/docs/service-accounts-actas for additional details.",
  "status" : "INVALID_ARGUMENT"
```

</details>

The way of specifying service account was wrong. -> `--serviceAccount=apache-beam-training@masatonaka1989.iam.gserviceaccount.com`

## References

1. https://chengzhizhao.medium.com/reading-apache-beam-programming-guide-3-pcollections-with-marvel-battle-stream-producer-2df5e6bbcabf
1. https://stackoverflow.com/questions/52972063/apache-beam-wont-write-files-to-local-env-or-google-storage
1. https://cloud.google.com/pubsub/docs/stream-messages-dataflow
1. https://beam.apache.org/get-started/quickstart-java/#run-wordcount-using-gradle
