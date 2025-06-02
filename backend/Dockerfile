# Build Stage
FROM maven:3.9.9-amazoncorretto AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM amazoncorretto:17

ARG PROFILE=prod
ARG APP_VERSION=1.0.0

WORKDIR /app

 # Copy images to be added via migration script and the jar file
COPY uploads ./uploads
COPY --from=build /build/target/resellmart-*.jar .

EXPOSE 8080

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}
ENV DB_URL=jdbc:mysql://mysql-resellmart:3306/resellmart_db

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} -Dspring.datasource.url=${DB_URL} resellmart-${JAR_VERSION}.jar