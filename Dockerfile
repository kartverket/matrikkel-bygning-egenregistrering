#FROM gradle:8-jdk21-alpine AS build
#COPY --chown=gradle:gradle . /home/gradle/src
#WORKDIR /home/gradle/src
#RUN gradle integrationTest --no-daemon

FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080:8080
RUN mkdir /app
COPY /build/libs/matrikkel-bygning-egenregistrering-all.jar /app/app.jar
ENTRYPOINT ["java", "-Dlogback.configurationFile=logback-cloud.xml", "-jar","/app/app.jar"]
