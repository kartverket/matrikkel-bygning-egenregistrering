FROM eclipse-temurin:21-alpine
RUN apk update && apk upgrade

EXPOSE 8080:8080
RUN mkdir /app
WORKDIR .
COPY build/libs/*.jar /app/matrikkel-bygning-egenregistering.jar
ENTRYPOINT ["java","-jar","/app/matrikkel-bygning-egenregistering.jar"]