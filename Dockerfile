FROM openjdk:11-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon build -x test pack

FROM openjdk:12-jdk-alpine as runner
WORKDIR /app
COPY --from=builder /app/build/libs/dwrtc-all-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 7000
CMD ["java", "-jar", "/app/app.jar"]
