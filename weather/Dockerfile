FROM alpine/java:21-jdk
RUN addgroup -S weather && adduser -S weather -G weather
USER weather:weather
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]