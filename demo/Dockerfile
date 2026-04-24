# Fase de compilación
FROM maven:3.9.6-eclipse-temurin-21 AS build
# Copiamos todo el contenido al contenedor
COPY . .
# Ejecutamos la compilación. Esto CREARÁ el archivo .jar
RUN mvn clean package -DskipTests

# Fase de ejecución
FROM eclipse-temurin:21-jre
# Buscamos el jar en cualquier subcarpeta target que exista
COPY --from=build /**/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]