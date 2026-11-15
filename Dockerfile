# syntax=docker/dockerfile:1

# ---- Étape 1 : compilation + tests unitaires (Maven, JDK 21) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src
# Le cache Maven est conservé entre deux builds pour ne pas tout retélécharger
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp package

# ---- Étape 2 : image d'exécution (JRE seul) ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home spring
COPY --from=build /app/target/app.jar ./app.jar
USER spring

EXPOSE 8080

# Forme "exec" : la JVM est le PID 1 et reçoit SIGTERM directement,
# ce qui déclenche l'arrêt propre (server.shutdown=graceful) lors d'un `docker stop`.
ENTRYPOINT ["java", "-jar", "app.jar"]
