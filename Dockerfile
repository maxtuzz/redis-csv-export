FROM ubuntu:18.04

WORKDIR /app

COPY target/app.jar /app

RUN apt-get update && apt-get install -y \
    pgloader \
    redis-tools \
    default-jdk

ENTRYPOINT ["java", "-jar", "app.jar"]