# SVG Image Converter ![build](https://github.com/nsclass/ns-svg-converter/actions/workflows/gradle-build.yml/badge.svg)

SVG image converter application made of Spring Boot 2.x.x and React for UI 


### SVG image converter ###

Main purpose of this project is to demonstrate building a reactive web application with SpringBoot 2.x.x.

#### Main Features
- Security with JWT token
- Cassandra data access
- React UI application with Spring boot framework

For data access layer, it is showing Cassandra DB design however it is configured to use memory based DAO by default. User can easily enable the Cassandra DAO by setting the active profile as dao_cassandra in application.yml file.

For automatic integration tests, this project is using Ruby Cucumber framework.

### Working application example ###

The following URL will demonstrate a working application. It will take some time to start up the application. So please be patient on opening this
URL.

https://svg-converter.herokuapp.com/

### Compile project ###

This application is using a Gradle as a build system and minimum required JDK is Java 8.

* Compiling UI project

The following gradle task will build and deploy the React application into Java backend project.

```
$ ./gradlew deployReactDist
```

Compiling React UI application

```$xslt
# cd ns-svg-converter-react
$ yarn build
``` 

* Compiling backend application

```
$ ./gradlew clean build
```

### Running application ###

* Compiling application including UI and running application

```
$ ./gradlew clean
$ ./gradlew deployReactDist build
$ java -jar ns-main-service/build/libs/ns-main-service-0.0.1-SNAPSHOT.jar
```

* Running application log

```$xslt
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.2.RELEASE)
```

* Browsing the application

```$xslt
http://localhost:8080/
```

### Creating docker container ###

Since ns-main-service project contains the Dockerfile, it can be run in Docker environment and Kubernetes cluster.

* Creating a container

```$xslt
$ cd ns-main-service
$ docker build -t [tagname] .
```

* Running a container

```$xslt
$ docker run -d --name=[name] -p 8080:8080 [image name]
```

### Running test ###

This project is using cucumber BDD testing framework.

In order to run the test, Ruby should be installed.

* Running test

```
$ ./gradlew bdd 
```

* BDD run result example

```
17 scenarios (17 passed)
135 steps (135 passed)
0m11.789s
```
