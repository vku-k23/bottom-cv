FROM maven:3.8.3-openjdk-17 as build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src /app/src
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:17-jre-alpine-3.21

RUN ln -sf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime

COPY --from=build /app/target/*.jar /run/app.jar

EXPOSE 8888

ENV JAVA_OPTIONS="-Xmx2048m -Xms256m"

ENTRYPOINT java $JAVA_OPTIONS -jar /run/app.jar