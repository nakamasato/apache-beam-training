/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:33.3.1-jre")

    // https://mvnrepository.com/artifact/org.apache.beam/beam-sdks-java-core
    implementation("org.apache.beam:beam-sdks-java-core:2.61.0")

    // https://cloud.google.com/pubsub/docs/publish-receive-messages-client-library#install
    implementation("org.apache.beam:beam-runners-google-cloud-dataflow-java:2.61.0")

    // https://mvnrepository.com/artifact/org.apache.beam/beam-examples-java
    implementation("org.apache.beam:beam-examples-java:2.61.0")

    // https://mvnrepository.com/artifact/org.apache.beam/beam-runners-direct-java
    runtimeOnly("org.apache.beam:beam-runners-direct-java:2.61.0")

    // https://mvnrepository.com/artifact/org.hamcrest/hamcrest
    testImplementation("org.hamcrest:hamcrest:3.0")
}

application {
    // Define the main class for the application.
    mainClass.set("apachebeamtraining.App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

if (project.hasProperty("dataflow-runner")) {
    dependencies {
        runtimeOnly("org.apache.beam:beam-runners-google-cloud-dataflow-java:2.61.0")
    }
}

task("execute", JavaExec::class) {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(System.getProperty("mainClass"))
}
