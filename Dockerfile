FROM maven:3.8.3-openjdk-17-slim as build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src /app/src
RUN mvn clean package -DskipTests=true

FROM gcr.io/distroless/java17-debian12

COPY --from=build /app/target/*.jar /app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app.jar"]