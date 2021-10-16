FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
WORKDIR /app
ADD "build/distributions/com.example.ktor-sample-0.1.0.tar" .
CMD ["com.example.ktor-sample-0.0.1/bin/com.example.ktor-sample"]
