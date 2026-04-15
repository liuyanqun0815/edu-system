FROM maven:3.8.8-eclipse-temurin-8 AS builder

WORKDIR /build
COPY . .
RUN mvn -T 1C -DskipTests clean package

FROM eclipse-temurin:8-jre

WORKDIR /app
COPY --from=builder /build/edu-admin/target/edu-admin-*.jar /app/app.jar

EXPOSE 8070

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
