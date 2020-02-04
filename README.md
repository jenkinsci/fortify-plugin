[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/fortify.svg)](https://wiki.jenkins.io/display/JENKINS/Fortify+Plugin)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=plugins/fortify-plugin/master)](https://ci.jenkins.io/job/plugins/job/fortify-plugin/job/master)

# Fortify Jenkins plugin

This is the official Jenkins plugin for Fortify Static Code Analyzer (SCA) and Fortify Software Security Center (SSC).

## Documentation

You can find plugin documentation here: https://www.microfocus.com/documentation/fortify-jenkins-plugin/

For more information about Fortify SCA, please visit https://www.microfocus.com/products/static-code-analysis-sast.

For more information about Fortify SSC, please visit https://www.microfocus.com/products/software-security-assurance-sdlc.

## Building and debugging

To build the plugin and connect your IDE for a remote debug session, you can use the following script:
```
SET MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
call mvn clean
call mvn package -Dssc.url=http://127.0.0.1:8180/ssc/
call mvn -Djetty.port=8181 -DskipTests=true hpi:run
```

## Usage notes

You must obtain a Fortify SSC authentication token to use the plugin's server related functionality, which includes build failure conditions and getting all vulnerability results in Jenkins.

* SSC authentication token (either JenkinsToken or CIToken). Token creation command:
  ```
  $ fortifyclient token -gettoken JenkinsToken -url http://localhost:8180/ssc -user admin
  ```
* Tests. Some of the junit tests can utilize a connection to Fortify Software Security Center to verify the plugin functionality.
  To override the default SSC location (localhost:8080), you can specify the optional SSC URL parameter: 'ssc.url'.
  For example:
  ```
  mvn package -Dssc.url=http://127.0.0.1[:port]/ssc/
  ```
  See other default parameters that you can override in the ssc.properties file.

The mvn command-line arguments are passed to Java as MAVEN_CMD_LINE_ARGS. Test cases will read this environment variable and set different URL and tokens during testing. Corresponding test cases are skipped if Fortify Software Security Center is not available.

