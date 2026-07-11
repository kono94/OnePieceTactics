FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jdk-alpine
VOLUME /tmp
WORKDIR /app
RUN mkdir -p /app/data
ARG APP_GIT_TAG=dev
ARG APP_GIT_COMMIT=unknown
ARG APP_BUILD_TIME=unknown
ENV APP_GIT_TAG=${APP_GIT_TAG}
ENV APP_GIT_COMMIT=${APP_GIT_COMMIT}
ENV APP_BUILD_TIME=${APP_BUILD_TIME}
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
