FROM gradle:8-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar

FROM eclipse-temurin:21-alpine
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/matrikkel-bygning-egenregistering.jar
ENTRYPOINT ["java","-jar","/app/matrikkel-bygning-egenregistering.jar"]