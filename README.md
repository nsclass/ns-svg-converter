# README #

SVG image converter application made of Reactive Spring Boot 2.x.x and React for UI

### SVG image converter ###

Main purpose of this project is to demonstrate how to use SpringBoot 2.x.x to build an reactive REST web application (
WebFlux) with JWT for security. Even UI is made of React Hook framework.

And this project includes Reactive Cassandra DAO to access Cassandra database but it is configured to use memory based
DAO by default. However it can be changed easily by setting the active profile as dao_cassandra in application.yml file.

Also, this project is using Ruby Cucumber framework for integration tests.

### Working application example ###

Because of a free Heroku account, it will require a time to start up the application. So please patient on opening this
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
