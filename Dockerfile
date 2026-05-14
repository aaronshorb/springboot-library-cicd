FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd -m myuser
COPY target/*.jar book-library.jar
USER myuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","book-library.jar"]
