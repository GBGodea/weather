FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/weather-1.0.0-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]