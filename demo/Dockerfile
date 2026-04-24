# Fase de compilación
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos todo el proyecto
COPY . .
# Forzamos la compilación desde la carpeta raíz
RUN mvn clean package -DskipTests

# Fase de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
# Buscamos CUALQUIER .jar que pese más de 10MB (el de Spring) y lo renombramos a app.jar
RUN --mount=type=bind,from=build,source=/app,target=/build \
    find /build -name "*.jar" -not -name "*sources*" -exec cp {} app.jar \;

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]