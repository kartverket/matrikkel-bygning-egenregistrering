FROM eclipse-temurin:21-jdk-alpine
ENV TZ=Europe/Oslo
EXPOSE 8080:8080
RUN mkdir /app
COPY /build/libs/matrikkel-bygning-egenregistrering-all.jar /app/app.jar
ENTRYPOINT ["java", "-Dlogback.configurationFile=logback-cloud.xml", "-jar","/app/app.jar"]
