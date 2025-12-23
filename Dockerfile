FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -B -q -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S spring  && adduser -S spring -G spring

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]