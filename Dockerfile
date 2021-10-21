FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
WORKDIR /app
ADD "build/distributions/ktor-sample-0.1.0.tar" .
CMD ["ktor-sample-0.0.1/bin/ktor-sample"]
