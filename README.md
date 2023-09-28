# SVG Image Converter ![build](https://github.com/nsclass/ns-svg-converter/actions/workflows/gradle-build.yml/badge.svg)

SVG image converter application made of Spring Boot 3.x.x and ReactJS for UI 

### SVG image converter ###

Main purpose of this project is to demonstrate building a reactive web application with SpringBoot 3.x.x. and ReactJS

#### Main Features
- React UI application with Spring boot framework
- WebFlux based REST API design
- Security with JWT token
- Open API documentation
- Cassandra data access
- Java Bean validation
- Dockerizing application
- Cucumber integration tests

For data access layer, it is configured to use memory based DAO by default but user can easily enable the Cassandra DAO by setting the active profile as dao_cassandra in application.yml file.

This project is using Ruby Cucumber framework for integration tests.

### Compile Project ###

Gradle is used for building application. Minimum required Java is JDK17.

* Compiling UI project

The following gradle task will build and deploy the React application to Java backend project.

```bash
$ ./gradlew deployReactDist
```

Compiling React UI application

```bash
$ cd ns-svg-converter-react
$ yarn build
``` 

* Compiling backend application

```bash
$ ./gradlew clean build
```

* Google formatting for backend application
Applied spotless plugin to format code and automatic copyright string.

```bash
$ ./gradlew spotlessApply                         
```

### Run Application ###

* Compiling application including UI

```bash
$ ./gradlew clean
$ ./gradlew deployReactDist build
$ java -jar ns-main-service/build/libs/ns-main-service-0.1.0-SNAPSHOT.jar
```

* Application log

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.3) 
```

* Browsing the application

```bash
http://localhost:8080/
```

### Open API and Swagger UI ###
Supporting Open API

- Open API docs
```bash
http://localhost:8080/v3/api-docs
```

- Swagger UI
```bash
http://localhost:8080/swagger-ui.html
```

### Docker Container Image ###

This project provides a Dockerfile to run an application in Docker container environment and Kubernetes cluster.

* Creating a Docker image

```bash
$ cd ns-main-service
$ docker build -t [tagname] .
```

* Running an application in Docker container

```bash
$ docker run --rm -d --name=[name] -p 8080:8080 [image name]
```

### Test ###

This project is using cucumber BDD testing framework.

Ruby should be installed to execute cucumber tests.

* Running test

```bash
$ ./gradlew bdd 
```

* BDD run result example

```bash
17 scenarios (17 passed)
135 steps (135 passed)
0m11.789s
```
