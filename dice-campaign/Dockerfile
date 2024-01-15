FROM openjdk:8-jdk-alpine
EXPOSE 9090
ARG JAR_FILE=target/dice-campaign-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} dice-campaign-0.0.1-SNAPSHOT.jar
COPY src/main/resources/truststore/caKafka.p12 /app/truststore/
RUN apk --no-cache add curl
RUN apk --no-cache add busybox-extras
RUN apk --no-cache add bash
ENTRYPOINT ["java","-jar","/dice-campaign-0.0.1-SNAPSHOT.jar"]