FROM java:openjdk-8-jre
MAINTAINER Hesham Massoud "hesham.massoud@commercetools.de"
COPY /build/libs/category-sync.jar /home/category-sync.jar
CMD ["java","-jar","/home/category-sync.jar"]