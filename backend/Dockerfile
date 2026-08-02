FROM maven:3.9-eclipse-temurin-25-alpine@sha256:72e2d64836e659d053a573ac9ebab05b78ae78fa7bb69b7452a7cb877b465fc7 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jdk-alpine@sha256:5ecfde8e5ecde5954ea3721155b345ef56c1d579b940c761318ad4c05959a151
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
ENTRYPOINT ["java","--enable-preview","-jar","app.jar"]
