# Dockerfile
FROM eclipse-temurin:11 as build

WORKDIR /app

# First, we need to download the dependencies for caching purposes
COPY mvnw pom.xml /app/
COPY .mvn /app/.mvn/
RUN ./mvnw dependency:go-offline

# Now build the application
COPY src /app/src/
RUN ./mvnw package

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /app/spring-petclinic.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/spring-petclinic.jar"]
