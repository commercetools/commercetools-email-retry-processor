# commercetools-email-retry-processor

Service which can be run as a cron job to ensure that in case of potential down time of an e-mail provider an e-mail can be send/re-tried asynchronously. 

Not succesfuly send e-mails are temporarily persisted as [Custom Objects](https://docs.commercetools.com/http-api-projects-custom-objects.html) in Commercetools Platform. For each email object, service triggers an HTTP call to a configured endpoint. API endpoint in turn should contain the e-mail delivery logic.

[![Build Status](https://travis-ci.org/commercetools/commercetools-email-retry-processor.svg?branch=create_cronjob)](https://travis-ci.org/commercetools/commercetools-email-retry-processor)
[![codecov](https://codecov.io/gh/commercetools/commercetools-email-retry-processor/branch/create_cronjob/graph/badge.svg)](https://codecov.io/gh/commercetools/commercetools-email-retry-processor)

## Prerequisites

 - Java 8
 - CTP project used for email deliveries.
 - [API endpoint](#api-endpoint), which triggers the email delivery logic

## Configuration

The configuration can be passed via environment variables or via a configuration file, as follows:

###  Configuration via environment variable:

Please set the following environment variable:

```
export CTP_PROJECT_CONFIG="{\"tenants\": [{\"projectKey\": \"<ctp project key>\",\"clientId\": \"<ctp project ID>\", \"clientSecret\": \"<ctp project client secret>\", \"endpointUrl\": \"<endpoint url>\", \"encryptionKey\" : \"<blowfish encryptionkey>\", \"processAll\" : <true|false>}]}"
```
###  Configuration via configuration file:

Please pass the path to the configuration file as argument to the "main" method

The configuration file should contain the following "JSON-SNIPPET".
 ```
 {
   "tenants": [
     {
             "projectKey": "<ctp project key>",
             "clientId": "<ctp project ID>",
             "clientSecret": "<ctp project client secret>",
             "endpointUrl": "<endpoint url>",
             "encryptionKey" : "<blowfish encryptionkey, which is used to encrypt the email object id before passing it the post request>",
             "processAll" : true|false <if true, all email objects (pending/error) are processed, otherwise only "pending" email objects will be processed (Default: false).>
           }
   ]
 }
   ```  


## Develop the application   

 Build the application and run tests
   ```bash
   ./gradlew clean build
   ```
## Run the application   

 - First, package the JAR
   ```bash
   ./gradlew clean shadowJar
   ```
   
 - Then run the JAR
   ```bash
   java -jar build/libs/email-processor.jar
   ```   

## API endpoint

The API endpoint should cover the following steps:

1. Fetch the current Email object by the given ID
1. After fetching it, send the email and process the result in the following way:
    - When the email delivery was successful
      - Delete current Email object
      - Set the Http status code "200" to the response
    - When the email delivery fails temporarily
      - Set the status of the Email object to "pending"
      - Set the Http status code "503" to the response
    - When the email delivery fails permanently.
      - Set the status of the Email object to "error"
      - Set the Http status code "400" to the response
