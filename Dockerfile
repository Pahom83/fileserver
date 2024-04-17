FROM openjdk:17-alpine3.12

EXPOSE 8080

COPY target/fileserver-0.0.1-SNAPSHOT.jar diplom.jar

ENTRYPOINT ["java", "-jar", "diplom.jar"]