# Jenkins Prometheus Metrics Plugin

## About
Jenkins Prometheus Plugin expose an endpoint (default `/prometheus`) with metrics where a Prometheus Server can scrape.

Documentation can be found [here](https://plugins.jenkins.io/prometheus)

Please note that the documentation is a WIP.

## Metrics exposed
2 types of metrics are exposed:

- Metrics from [Metrics-plugin](https://github.com/jenkinsci/metrics-plugin)
- Metrics from this plugin. Refer [Prometheus-plugin](docs/metrics/index.md)

## Configuring the plugin
You can find some examples in this documentation [Configuring Plugin](docs/configuration/configuration.md)

## Environment variables

`PROMETHEUS_NAMESPACE` Prefix of metric (Default: `default`).

`PROMETHEUS_ENDPOINT` REST Endpoint (Default: `prometheus`)

`COLLECTING_METRICS_PERIOD_IN_SECONDS` Async task period in seconds (Default: `120` seconds)

`COLLECT_DISK_USAGE` Should the plugin collect disk usage information. Set this to false if you are running Jenkins against a cloud-based storage backend, in order to avoid scanning virtually unlimited storage.


## Building

    mvn clean install
    mvn hpi:hpi

## Author / Maintainer

[Waschndolos](https://github.com/waschndolos)

[Marky Jackson](https://github.com/markyjackson-taulia)

# Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Opening or finding an issue
OPENING AN ISSUE:
You should usually open an issue in the following situations:

Report an error you can’t solve yourself
Discuss a high-level topic or idea (for example, community, vision or policies)
Propose a new feature or other project idea

FINDING AN ISSUE:
If you found an open issue that you want to tackle, comment on the issue to let people know you’re on it. That way, people are less likely to duplicate your work.

If an issue was opened a while ago, it’s possible that it’s being addressed somewhere else, or has already been resolved, so comment to ask for confirmation before starting work.

## Opening a pull request
You should usually open a pull request in the following situations:

Submit trivial fixes (for example, a typo, a broken link or an obvious error)
Start work on a contribution that was already asked for, or that you’ve already discussed, in an issue

### Testing your code
To run unit tests, use the `test` maven goal, or
```shell
mvn test
```

The automated pipeline also runs static analysis, to run it locally, use the `spotbugs:check` target, or
```shell
mvn spotbugs:check
```

### Forking a repository
Fork the repository and clone it locally. Connect your local to the original “upstream” repository by adding it as a remote. Pull in changes from “upstream” often so that you stay up to date so that when you submit your pull request, merge conflicts will be less likely.

Create a branch for your edits.

Reference any relevant issues or supporting documentation in your PR (for example, “Closes #37.”)

Include screenshots of the before and after if your changes include differences in HTML/CSS. Drag and drop the images into the body of your pull request.

Test your changes! Run your changes against any existing tests if they exist and create new ones when needed. Whether tests exist or not, make sure your changes don’t break the existing project.

Getting Started
Fork this repository on GitHub by clicking the Fork button in the top right of this page.

Clone your forked repo to your local machine.

Create a new branch.
git checkout -b new-branch

Add your contributions.
Have a look at CONTRIBUTING.md. There are 3 easy ways to contribute to this project:

Commit and push your changes.

git add -A 
git commit -m "Your commit message"
git push --set-upstream origin new-branch
Create a Pull Request by navigating to your forked repository and clicking the New pull request button on your left-hand side of the page.

Add in a title, edit the PR template, and then press the Create pull request button.

Wait for your Pull Request to be reviewed and merged.

### Congratulations! You just opened a Pull Request.


