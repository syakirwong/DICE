FROM openjdk:17-alpine3.14
EXPOSE 9090
ARG JAR_FILE=target/dice-etl-0.0.1-SNAPSHOT.jar
RUN mkdir /ssl && chmod 777 /ssl
COPY UATAOPDBSSL.jks /ssl/UATAOPDBSSL.jks
COPY ${JAR_FILE} dice-etl-0.0.1-SNAPSHOT.jar
RUN apk --no-cache add curl busybox-extras bash
ENTRYPOINT ["java","-jar","/dice-etl-0.0.1-SNAPSHOT.jar"]