FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk
COPY --from=build /target/ai-email-reply-0.0.1-SNAPSHOT.jar app.jar
EXPOSE  8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]