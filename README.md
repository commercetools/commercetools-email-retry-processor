# commercetools-email-retry-processor

 The email retry processor fetches all emailobjects, which are saved in customobjects,  of the given tenants. For each
 of this emailobjects, the processor checks, if they are in state "pending" and ,if true, it triggers a api endpoint.
 This api endpoint should contain the email delivery logic.

[![Build Status](https://travis-ci.org/commercetools/commercetools-email-retry-processor.svg?branch=create_cronjob)](https://travis-ci.org/commercetools/commercetools-email-retry-processor)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Prerequisites

 - Java 8
 - CTP project, for which the email should be delivered
 - [API endpoint](#api-endpoint), which triggers the email delivery logic

## Configuration

The configuration can be passed via environment variables or via a configuration file, as follows:

##  Configuration via environment variable:

Please set the following environment variable:

```
export CTP_PROJECT_CONFIG="{\"tenants\": [{\"projectKey\": \"<ctp project key>\",\"clientId\": \"<ctp project ID>\", \"clientSecret\": \"<ctp project client secret>\", \"endpointUrl\": \"<endpoint url>"}]}"
```
##  Configuration via configuration file:

Please pass the path to the configuration file as argument to the "main" method

The configuration file should contain the following "JSON-SNIPPET".
 ```
 {
   "tenants": [
     {
       "projectKey": "<ctp project key>",
       "clientId": "<ctp project ID>",
       "clientSecret": "<ctp project client secret>",
       "endpointUrl": "<endpoint url>"
     }
   ]
 }
   ```  

## Run the application   

 - First, package the JAR
   ```bash
   ./gradlew clean jar
   ```
 - Then run the JAR
   ```bash
   java -jar build/libs/email-processor.jar
   ```   

## API endpoint

The API endpoint should cover the following steps:

1. Fetch the current Emailobject by the given ID
1.  Check if the Emailobject is in state "pending"
1. If true, send the emai:
    - When the email delivery was successful
      - Delete current Emailobject
      - Set the Httpstatuscode "200" to the response
    - When the email delivery fails temporarily
      - Set the Httpstatuscode "503" to the response
    - When the email delivery fails permanently.
    - Set the status of the emailobject to "error"
      - Set the Httpstatuscode "400" to the response
