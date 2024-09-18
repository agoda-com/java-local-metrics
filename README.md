# java-local-metrics: Measure Your F5 Experience!

## Overview

Welcome to java-local-metrics, the open-source library that's here to make your development experience smoother than a fresh jar of skippy! We're all about measuring and improving the developer experience on local workstations, because let's face it, waiting for your tests to run is about as fun as watching paint dry.

By providing insights into test execution, build times, and system resource usage, this library helps development teams identify bottlenecks, optimize workflows, and enhance overall productivity. Because who doesn't want to be more productive while writing Java, Kotlin, and Scala?

What is the F5 Experience? have a read [here](https://beerandserversdontmix.com/2024/08/15/an-introduction-to-the-f5-experience/)

## Latest Versions

Check out our latest versions. They're fresher than your morning coffee!

- **[Maven Central JUnit](https://central.sonatype.com/artifact/io.agodadev/testmetrics)**
- **[Maven Central Scalatest](https://central.sonatype.com/artifact/io.agodadev/scala-test-metrics)**

## Features

- **Test Execution Metrics**: Capture detailed information about JUnit and ScalaTest runs, including execution time, pass/fail status, and system resource usage during tests. It's like a fitbit for your tests!
- **The F5 Experience**: We're all about that smooth, fast development cycle. Press F5 and watch your productivity soar!

## Requirements

Minimum Java Version: 11 (Because we believe in moving forward, not living in the past)

## Installation

### Maven (For Java and Kotlin)

Add this to your `pom.xml`. It's like seasoning for your project!

```xml
<dependency>
    <groupId>io.agodadev.testmetrics</groupId>
    <artifactId>dev-experience-metrics</artifactId>
    <version>1.0.x</version>
</dependency>
```

### Gradle (For Java and Kotlin)

Spice up your `build.gradle` with this:

```gradle
implementation 'io.agodadev.testmetrics:dev-experience-metrics:1.0.x'
```

### Scala

Add some flavor to your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  // other dependencies here
  "io.agodadev" %% "scala-test-metrics" % "1.0.x",
  // other dependencies here
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-C", "io.agodadev.testmetricsscala.TestMetricsReporter")
```

## Quick Start

### For JUnit Lovers

Add this VM option to your JUnit run configuration. It's like giving your tests a superpower!

```shell
-Djunit.runner.addListeners=io.agodadev.testmetrics.LocalCollectionTestRunListener
```

## Contributing

We welcome contributions! Whether you're fixing bugs, improving documentation, or adding new features, we appreciate your help in making java-local-metrics even better. Check out our [Contributing Guide](CONTRIBUTING.md) for more details on how to get started.

Remember, in the world of java-local-metrics, there are no stupid questions, only stupid build times!

## The F5 Experience

We're all about that F5 Experience here at java-local-metrics. Our goal is to make your development process smoother than a freshly waxed surfboard. Here's what that means for you:

1. **Setup Should Be a Breeze**: You should be able to clone the repo and get up and running faster than you can say "Java-Kotlin-Scala".
2. **Fast Feedback Loop**: We want our tests to run faster than a caffeinated squirrel. If you find yourself waiting for tests, something's wrong.

## And Finally...

Remember, in software development, there are only two types of projects: those that are measuring their build times, and those that are not finished yet. With java-local-metrics, you'll always know exactly how long you're waiting for your tests. (Spoiler alert: with our help, it won't be long!)

Happy coding, and may your F5 key never wear out! 🚀