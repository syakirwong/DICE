FROM openjdk:8-jdk-alpine
EXPOSE 9090
ARG JAR_FILE=target/dice-analytics-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} dice-analytics-0.0.1-SNAPSHOT.jar
RUN apk --no-cache add curl
RUN apk --no-cache add busybox-extras
RUN apk --no-cache add bash
RUN apk add ttf-dejavu
ENTRYPOINT ["java","-jar","/dice-analytics-0.0.1-SNAPSHOT.jar"]