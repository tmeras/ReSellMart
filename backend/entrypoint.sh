#!/bin/sh

exec java -jar \
  -Dspring.profiles.active=${ACTIVE_PROFILE} \
  -Dspring.datasource.url=${DB_URL} \
  resellmart-${JAR_VERSION}.jar