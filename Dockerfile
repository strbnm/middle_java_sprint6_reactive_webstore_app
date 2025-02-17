# Используем официальный образ Tomcat
FROM eclipse-temurin:21-jammy

RUN addgroup --system app && adduser --system --group app
RUN mkdir -p /opt/images

WORKDIR /opt/app

COPY build/libs/sprint5-practicum-homework-1.0.0.jar app.jar
COPY build/resources/main/static/uploads/*.jpg /opt/images

RUN chmod +x app.jar && chown -R app:app /opt/images /opt/app

EXPOSE 8080

USER app

CMD ["./app.jar"]
