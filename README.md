# commercetools-email-retry-processor

 The email retry processor fetches all email objects, which are saved as Custom Objects in CTP for different tenants/webshops. For each email object, this job triggers an api endpoint.
 The api endpoint should contain the email delivery logic.

[![Build Status](https://travis-ci.org/commercetools/commercetools-email-retry-processor.svg?branch=create_cronjob)](https://travis-ci.org/commercetools/commercetools-email-retry-processor)


## Prerequisites

 - Java 8
 - CTP project used for email deliveries.
 - [API endpoint](#api-endpoint), which triggers the email delivery logic

## Configuration

The configuration can be passed via environment variables or via a configuration file, as follows:

##  Configuration via environment variable:

Please set the following environment variable:

```
export CTP_PROJECT_CONFIG="{\"tenants\": [{\"projectKey\": \"<ctp project key>\",\"clientId\": \"<ctp project ID>\", \"clientSecret\": \"<ctp project client secret>\", \"endpointUrl\": \"<endpoint url>\", \"encryptionKey\" : \"<blowfish encryptionkey>\", \"processAll\" : <true|false>}]}"
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
             "endpointUrl": "<endpoint url>",
             "encryptionKey" : "<blowfish encryptionkey>",
             "processAll"    : <if true, all email objects (pending/error) are processed, otherwise only "pending" email objects will be processed (Default: false).>
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

1. Fetch the current Email object by the given ID
1. If true, send the email:
    - When the email delivery was successful
      - Delete current Email object
      - Set the Http status code "200" to the response
    - When the email delivery fails temporarily
      - Set the Http status code "503" to the response
    - When the email delivery fails permanently.
      - Set the status of the Email object to "error"
      - Set the Http status code "400" to the response
1. If false, the Email object should be ignored