FROM gradle:8-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowjar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
ENV TZ=Europe/Oslo
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/matrikkel-bygning-egenregistrering.jar
ENTRYPOINT ["java", "-Dlogback.configurationFile=logback-cloud.xml", "-jar","/app/matrikkel-bygning-egenregistrering.jar"]