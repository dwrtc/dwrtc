FROM openjdk:11 as builder
WORKDIR /app
COPY . .
RUN ./gradlew -s --no-daemon --console plain build -x test pack

FROM openjdk:11-slim as runner
WORKDIR /app
COPY --from=builder /app/build/libs/dwrtc-all-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 7000
CMD ["java", "-jar", "/app/app.jar"]
