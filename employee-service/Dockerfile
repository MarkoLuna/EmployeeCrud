FROM gradle:jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM adoptopenjdk/openjdk11:jre-11.0.6_10
EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

### commands
# docker build -t employeeapp .
# docker run -p 8080:8080 employeeapp
