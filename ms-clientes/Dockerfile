# ============================================================
#  Etapa 1: compilación (imagen con Maven + JDK)
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: las dependencias quedan en caché de Docker
# y no se vuelven a descargar si solo cambia el código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ============================================================
#  Etapa 2: ejecución (imagen liviana, solo JRE)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: el proceso no corre como root
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]