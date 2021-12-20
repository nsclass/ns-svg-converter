# SVG Image Converter ![build](https://github.com/nsclass/ns-svg-converter/actions/workflows/gradle-build.yml/badge.svg)

SVG image converter application made of Spring Boot 2.x.x and ReactJS for UI 

### SVG image converter ###

Main purpose of this project is to demonstrate building a reactive web application with SpringBoot 2.x.x. and ReactJS

#### Main Features
- Security with JWT token
- Cassandra data access
- React UI application with Spring boot framework

For data access layer, it is showing Cassandra DB design however it is configured to use memory based DAO by default. User can easily enable the Cassandra DAO by setting the active profile as dao_cassandra in application.yml file.

For automatic integration tests, this project is using Ruby Cucumber framework.

### Application Demo ###

The following URL will demonstrate a live working application. It will take some time to start up the application. So please be patient on opening the first time

https://svg-converter.herokuapp.com/

### Compile Project ###

This application is using a Gradle as a build system and minimum required JDK is Java 11.

* Compiling UI project

The following gradle task will build and deploy the React application into Java backend project.

```bash
$ ./gradlew deployReactDist
```

Compiling React UI application

```bash
# cd ns-svg-converter-react
$ yarn build
``` 

* Compiling backend application

```bash
$ ./gradlew clean build
```

### Run Application ###

* Compiling application including UI

```bash
$ ./gradlew clean
$ ./gradlew deployReactDist build
$ java -jar ns-main-service/build/libs/ns-main-service-0.0.1-SNAPSHOT.jar
```

* Application log

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.2.RELEASE)
```

* Browsing the application

```bash
http://localhost:8080/
```

### Creating docker container ###

Since ns-main-service project contains the Dockerfile, it can be run in Docker container environment and Kubernetes cluster.

* Creating a container

```bash
$ cd ns-main-service
$ docker build -t [tagname] .
```

* Running a container

```bash
$ docker run --rm -d --name=[name] -p 8080:8080 [image name]
```

### Test ###

This project is using cucumber BDD testing framework.

In order to run the test, Ruby should be installed.

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
