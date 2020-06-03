FROM openjdk:8-jre
MAINTAINER andreas Halberkamp "andreas.halberkamp@commercetools.de"
COPY /build/libs/email-processor.jar /home/email-processor.jar
CMD ["java","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-jar","/home/email-processor.jar"]
