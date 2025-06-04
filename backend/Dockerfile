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

# Copy the jar file and the images that are referenced in the migration script
COPY uploads ./uploads
COPY --from=build /build/target/resellmart-*.jar .

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}
ENV DB_URL="jdbc:mysql://mysql-resellmart:3306/resellmart_db?allowPublicKeyRetrieval=true&useSSL=false"

EXPOSE 8080

# Copy the entrypoint script and make it executable
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]