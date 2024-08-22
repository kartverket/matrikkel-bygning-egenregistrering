FROM eclipse-temurin:21-jdk-alpine
ENV TZ=Europe/Oslo
EXPOSE ${PORT:-8080}:${PORT:-8080}
RUN mkdir /app
COPY /build/libs/matrikkel-bygning-egenregistrering-all.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
