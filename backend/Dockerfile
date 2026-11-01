# =====================================================================
#  Dockerfile de TechStore API (lo usa Render para construir y ejecutar)
#
#  "Multi-stage" = dos etapas:
#   1) BUILD:   una imagen con el JDK completo compila el proyecto y genera el .jar
#   2) RUNTIME: una imagen más liviana, solo con el JRE, ejecuta ese .jar
#  Así la imagen final no lleva Maven, el código fuente ni el compilador.
# =====================================================================

# ---------- Etapa 1: construir el .jar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero copiamos SOLO lo necesario para descargar dependencias.
# Docker guarda cada paso en caché: si el pom.xml no cambia, no vuelve a descargarlas.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# chmod +x: en Windows, Git guarda mvnw sin permiso de ejecución; aquí se lo damos.
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Ahora sí, el código fuente, y compilamos (sin pruebas, para que sea más rápido)
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios: si alguien lograra atacar la app, no sería "root" en el contenedor
RUN groupadd --system app && useradd --system --gid app --no-create-home app
USER app

# Copiamos únicamente el .jar generado en la etapa anterior
COPY --from=build /app/target/techstore-api-1.0.0.jar app.jar

# Puerto por defecto. En Render la app usa la variable PORT (ver application.yml)
EXPOSE 8080

# -XX:MaxRAMPercentage=75 : Java usa como máximo el 75 % de la RAM del contenedor
#                            (el plan gratuito de Render tiene 512 MB).
# -XX:+ExitOnOutOfMemoryError : si se queda sin memoria, se cierra y Render lo reinicia
#                            (mejor que quedarse "colgado").
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
