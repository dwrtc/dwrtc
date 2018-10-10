FROM gradle:jdk10 as builder
WORKDIR /app
COPY . .
USER root
RUN ./gradlew --no-daemon build pack


FROM openjdk:12-jdk-alpine as runner
WORKDIR /app
COPY --from=builder /app/build/libs/dwrtc-all-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 7000
CMD ["java", "-jar", "/app/app.jar"]
