# Repository Guidelines

## Project Structure

This repository builds the Jenkins Prometheus metrics plugin as a single Maven module. Java code is under `src/main/java/org/jenkinsci/plugins/prometheus/`. Metrics live in `collectors/`, grouped by area such as builds, jobs, executors, and coverage. The `service/` package runs collection, `rest/PrometheusAction.java` serves the scrape endpoint, and `config/` holds plugin settings and metric filters. Jelly forms, help files, and message bundles live under `src/main/resources/`. Tests mirror the production packages under `src/test/java/`.

## Build and Run Locally

Use Maven with JDK 21 or 25. The `Jenkinsfile` tests Linux on both JDKs and Windows on JDK 25. Check `pom.xml` for the current Jenkins baseline.

- `mvn clean install` builds, tests, packages, and installs the plugin in the local Maven repository.
- `mvn hpi:hpi` packages the plugin as an HPI in `target/`.
- `mvn hpi:run` starts a local Jenkins instance. Try the default scrape endpoint at `http://localhost:8080/jenkins/prometheus/`.
- `mvn spotbugs:check` runs the static analysis check documented in the README.

## Java Style and Compatibility

Use four spaces for Java indentation and match the surrounding import and brace style. Follow existing names such as `CurrentRunDurationGauge` and `JobCollectorFactory`. Treat exported metric names and labels as compatibility-sensitive because dashboards and alerts query them directly. Update the README or corresponding Jelly help when changing user-facing settings.

## Testing

Name test classes `*Test` and put them in the package of the code they cover. Use JUnit 5 and Mockito for unit tests, and the Jenkins test harness when behavior needs a running controller. Cover changed behavior and regressions. Run a focused test with `mvn -Dtest=JobCollectorTest test` and the full suite with `mvn test` before submitting.

## Commits and Pull Requests

Work on a topic branch and make focused commits with short, imperative subjects, such as “Check maxBuilds bound before advancing to previous Run”. Run `git diff --check` before committing. Discuss major changes in an issue first. Fill out the PR template, summarize the change and how it was tested, and link relevant issues with wording such as `Fixes #123`. Include before-and-after screenshots for HTML or CSS changes. See [Contributing](.github/CONTRIBUTING.md).
