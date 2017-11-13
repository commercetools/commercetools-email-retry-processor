# commercetools-email-retry-processor
Scheduled service which allows to send emails asynchronously and retry on errors.




- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [Run the application](#run-the-application)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Usage
### Prerequisites
 
 - install Java 8
 - CTP projects for which the email should be delivery
 - set the following environment variable, before running the job
   ```bash
   export CTP_PROJECT_CONFIG = <JSON SNIPPET> 
   ```
 
 Example configuration: 
 
```
 {
   "tenants": [
     {
       "projectKey": "project-key",
       "clientId": "XXX",
       "clientSecret": "XXX",
       "webhookURL": "http://www.<host>:<port>/<context-Path>"
     }
   ]
 }
   ```  
   
### Run the application   
 - First, package the JAR
   ```bash
   ./gradlew clean jar
   ```
 - Then run the JAR
   ```bash
   java -jar build/libs/category-sync.jar
   ```   