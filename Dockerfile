FROM openjdk:8-jre-alpine
MAINTAINER andreas Halberkamp "andreas.halberkamp@commercetools.de"
COPY /build/libs/email-processor.jar /home/email-processor.jar
CMD ["java","-jar","/home/email-processor.jar"]
