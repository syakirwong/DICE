FROM openjdk:8-jdk-alpine
EXPOSE 9090
ARG JAR_FILE=target/dice-integration-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} dice-integration-0.0.1-SNAPSHOT.jar
RUN apk --no-cache add curl
RUN apk --no-cache add busybox-extras
RUN apk --no-cache add bash
ENTRYPOINT ["java","-jar","/dice-integration-0.0.1-SNAPSHOT.jar"]