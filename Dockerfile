# syntax=docker/dockerfile:1

# Étape 1 : compile l'application et lance les tests unitaires
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src
# Garde les dépendances Maven en cache d'une construction à l'autre
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp package

# Étape 2 : image finale, avec seulement ce qu'il faut pour lancer l'application
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home spring
COPY --from=build /app/target/app.jar ./app.jar
USER spring

EXPOSE 8080

# Lancée ainsi, l'application reçoit le signal de `docker stop` et s'arrête proprement
ENTRYPOINT ["java", "-jar", "app.jar"]
