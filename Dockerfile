FROM eclipse-temurin:21-jdk-alpine

ENV USER_ID=150 \
    USER_NAME=apprunner \
    TZ=Europe/Oslo

RUN addgroup -g ${USER_ID} ${USER_NAME} \
    && adduser -u ${USER_ID} -G ${USER_NAME} -D ${USER_NAME}

COPY --chown=${USER_ID}:${USER_ID} /web/build/libs/app.jar /app/app.jar

USER ${USER_NAME}
EXPOSE ${PORT:-8080}:${PORT:-8080}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
