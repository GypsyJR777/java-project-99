FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
COPY docker-entrypoint.sh docker-entrypoint.sh

EXPOSE 10000

RUN chmod +x docker-entrypoint.sh

ENTRYPOINT ["./docker-entrypoint.sh"]
