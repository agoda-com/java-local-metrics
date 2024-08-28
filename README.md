# Developer Experience Metrics Library

## Overview

The Developer Experience Metrics Library is an open-source Java tool designed to measure and improve the developer experience on local workstations. By providing insights into test execution, build times, and system resource usage, this library helps development teams identify bottlenecks, optimize workflows, and enhance overall productivity.

## Features

- **Test Execution Metrics**: Capture detailed information about JUnit test runs, including execution time, pass/fail status, and system resource usage during tests.

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.agodadev.testmetrics</groupId>
    <artifactId>dev-experience-metrics</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
implementation 'io.agodadev.testmetrics:dev-experience-metrics:1.0.0'
```

## Quick Start


### 1. Configure JUnit Listener

Add the following VM option to your JUnit run configuration:

```
-Djunit.runner.addListeners=io.agodadev.testmetrics.LocalCollectionTestRunListener
```

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for more details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to all the contributors who have helped shape this project.
- Special thanks to the JUnit team for their excellent testing framework.

---

Happy coding, and may your development experience be ever improving!