# syntax=docker/dockerfile:1.7

FROM docker.io/library/eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && java -Djarmode=tools -jar target/*.jar extract --layers --destination target/extracted --application-filename application.jar

FROM docker.io/library/eclipse-temurin:25-jre-alpine
RUN addgroup -S granthalay && adduser -S granthalay -G granthalay
WORKDIR /app

COPY --from=build --chown=granthalay:granthalay /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=granthalay:granthalay /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=granthalay:granthalay /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=granthalay:granthalay /workspace/target/extracted/application/ ./

USER granthalay
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
