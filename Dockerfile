FROM gradle:jdk11-slim as builder
USER root
WORKDIR /app
COPY . .
RUN gradle -s --no-daemon --console plain build -i -x test pack

FROM openjdk:11-slim as runner
WORKDIR /app
COPY --from=builder /app/build/libs/dwrtc-all-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 7000
CMD ["java", "-jar", "/app/app.jar"]
