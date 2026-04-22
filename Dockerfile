# Giai đoạn 1: Build ứng dụng
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy file jar từ giai đoạn build sang giai đoạn chạy
COPY --from=build /app/target/*.jar app.jar

# Cấu hình cổng mặc định mà Spring Boot lắng nghe (thường là 8080)
EXPOSE 8081

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]