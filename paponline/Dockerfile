# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .

# Converte eventuais CRLF em LF e dá permissão de execução
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew build -x test --no-daemon

# Etapa 2: Execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]