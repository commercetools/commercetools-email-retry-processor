# commercetools-email-retry-processor
Scheduled service which allows to send emails asynchronously and retry on errors.

[![Build Status](https://travis-ci.org/commercetools/commercetools-email-retry-processor.svg?branch=create_cronjob)](https://travis-ci.org/commercetools/commercetools-email-retry-processor)


- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Run the application](#run-the-application)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Usage
### Prerequisites
 
 - install Java 8
 - CTP projects for which the email should be delivery
 
 
 ### Configuration
 
The configuration can be passed by enviroment variable or by a configurationfile to the processor


####  Example configuration: 
 
 This JSON-SNIPPET contains the configuration of one tenant.
 ```
 {
   "tenants": [
     {
       "projectKey": "project-key",
       "clientId": "XXX",
       "clientSecret": "XXX",
       "apiEndpointURL": "http://www.<host>:<port>/<context-Path>" // URL To the api endpoind, which triggers the email delivery logiic.
     }
   ]
 }
   ```  

#### Using the enviroment variable
 
  - Just  set the following environment variable, before running the job
   ```bash
   export CTP_PROJECT_CONFIG = <JSON SNIPPET> 
   ```

#### Using Config file

- Just add the configfile path to the service call


## Run the application   
 
 - First, package the JAR
   ```bash
   ./gradlew clean jar
   ```
 - Then run the JAR
   ```bash
   java -jar build/libs/category-sync.jar
   ```   
