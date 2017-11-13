# Contributing

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Development](#development)
    - [Run unit tests](#run-unit-tests)
    - [Package JARs](#package-jars)
    - [Package JARs and run tests](#package-jars-and-run-tests)
    - [Full build with tests, but without install to maven local repo (Recommended)](#full-build-with-tests-but-without-install-to-maven-local-repo-recommended)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


- Every PR should address an issue on the repository or JIRA. If the issue doesn't exist, please create it first.
- Pull requests should always follow the following naming convention: 
`[issue-number]-[pr-name]`. 
- Commit messages should always be prefixed with the number of the issue that they address. 
For example, `CDL-2055: Remove redundant space.`
- After your PR is merged to master:
    - Delete the branch.
    - Mark the issue it addresses with the `merged-to-master` label.
    - Close the issue **only** if the change was released.

### Development
##### Run unit tests
````bash
./gradlew test
````

##### Package JARs
````bash
./gradlew clean jar
````

##### Package JARs and run tests
````bash
./gradlew clean check
````

##### Full build with tests, but without install to maven local repo (Recommended)
````bash
./gradlew clean build
````
