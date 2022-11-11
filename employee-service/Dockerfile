FROM amazoncorretto:17-al2022-jdk
EXPOSE 8080

COPY target/EmployeeService.jar /app.jar
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

### commands
# docker build -t employeeapp .
# docker run -p 8080:8080 --rm employeeapp
