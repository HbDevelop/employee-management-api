# syntax=docker/dockerfile:1

# Étape 1 : compile l'application et lance les tests unitaires
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Fichiers de build d'abord : tant qu'ils ne changent pas, Docker réutilise cette étape
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
# Garde Gradle et les dépendances en cache d'une construction à l'autre
RUN --mount=type=cache,target=/root/.gradle ./gradlew test bootJar --no-daemon

# Étape 2 : image finale, avec seulement ce qu'il faut pour lancer l'application
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home spring
COPY --from=build /app/build/libs/app.jar ./app.jar
USER spring

EXPOSE 8080

# Lancée ainsi, l'application reçoit le signal de `docker stop` et s'arrête proprement
ENTRYPOINT ["java", "-jar", "app.jar"]
