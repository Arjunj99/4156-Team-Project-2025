FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY data ./data

#download all dependencies
RUN mvn dependency:go-offline

EXPOSE 8080

#run the server
CMD ["mvn", "spring-boot:run"]

