# commercetools-email-retry-processor

[![Build Status](https://travis-ci.org/commercetools/commercetools-email-retry-processor.svg?branch=create_cronjob)](https://travis-ci.org/commercetools/commercetools-email-retry-processor)
[![codecov](https://codecov.io/gh/commercetools/commercetools-email-retry-processor/branch/create_cronjob/graph/badge.svg)](https://codecov.io/gh/commercetools/commercetools-email-retry-processor)

Typically run as a cron job to ensure that an e-mail is sent (and re-tried) asynchronously, in case of potential down time of an e-mail provider. 

- This application sends an HTTP request to a configured endpoint. 
This configured API endpoint should contain the e-mail delivery logic. More info on what the configured endpoint should do is described [here](#api-endpoint).

- This application expects e-mails that failed to be sent, to be persisted as [Custom Objects](https://docs.commercetools.com/http-api-projects-custom-objects.html) in the commercetools Platform. 

- This application supports multi-tenant configurations. In other words, it can be used for multiple applications, each with their own commercetools project for persistence.

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

1. Fetch the current e-mail object by the given id (commercetools CustomObject UUID)
1. After fetching it, send the e-mail and process the result in the following way:
    - If the e-mail delivery is successful
      - Delete the current e-mail object
      - Set the response Http status code to `200`
    - If the e-mail delivery fails for a temporary reason (e.g. connection error, slow network, etc..)
      - Set the status of the e-mail CustomObject to `pending`
      - Set the response Http status code to `503`
    - If the e-mail delivery fails for a permanent reason (e.g. wrong e-mail address, etc..)
      - Set the status of the e-mail CustomObject to `error`
      - Set the response Http status code to `400`
