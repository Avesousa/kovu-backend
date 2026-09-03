# --- Etapa de build: compila y empaqueta con Maven sobre JDK 25 ---
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /build

# Copiamos el pom primero para que Docker cachee las dependencias
# descargadas mientras el código fuente siga cambiando.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Etapa de runtime: solo el JRE + el jar sombreado, nada de Maven ---
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/kovu-backend.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
