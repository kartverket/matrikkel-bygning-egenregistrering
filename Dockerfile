FROM eclipse-temurin:21-jdk-alpine@sha256:cafcfad1d9d3b6e7dd983fa367f085ca1c846ce792da59bcb420ac4424296d56

ENV USER_ID=150 \
    USER_NAME=apprunner \
    TZ=Europe/Oslo

RUN addgroup -g ${USER_ID} ${USER_NAME} \
    && adduser -u ${USER_ID} -G ${USER_NAME} -D ${USER_NAME}

COPY --chown=${USER_ID}:${USER_ID} /web/build/libs/app.jar /app/app.jar

USER ${USER_NAME}
EXPOSE ${PORT:-8080}:${PORT:-8080}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
