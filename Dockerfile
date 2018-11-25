FROM gradle:jdk11-slim as builder
USER root
ENV GRADLE_USER_HOME /app/.gradle
WORKDIR /app
COPY *.gradle ./
RUN gradle -s --no-daemon --console plain build
COPY . .
RUN gradle -s --no-daemon --console plain build -x test pack

FROM openjdk:11-slim as runner
WORKDIR /app
COPY --from=builder /app/build/libs/dwrtc-all-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 7000
CMD ["java", "-jar", "/app/app.jar"]
