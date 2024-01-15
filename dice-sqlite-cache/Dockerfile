FROM openjdk:17-alpine3.14
EXPOSE 9096
# Create a directory named "data" and set permissions to 777
RUN mkdir /data && chmod 777 /data
ARG JAR_FILE=target/dice-sqlite-cache-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} dice-sqlite-cache-0.0.1-SNAPSHOT.jar
RUN apk --no-cache add curl busybox-extras bash
ENTRYPOINT ["java","-jar","/dice-sqlite-cache-0.0.1-SNAPSHOT.jar"]

#FROM openjdk:17-alpine3.14
#EXPOSE 9096
#ARG JAR_FILE=target/dice-sqlite-cache-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} dice-sqlite-cache-0.0.1-SNAPSHOT.jar
#RUN apk --no-cache add curl
#RUN apk --no-cache add busybox-extras
#RUN apk --no-cache add bash
#ENTRYPOINT ["java","-jar","/dice-sqlite-cache-0.0.1-SNAPSHOT.jar"]