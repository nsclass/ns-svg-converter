# README #

SVG image converter application made of Spring Boot 2.x.x and Angular framework for UI

### SVG image converter ###

Main purpose of this project is to demonstrate how to use SpringBoot 2.1.x with enabling JWT based security
and REST API for building Web application with Angular framework.

This project also includes Cassandra DAO as a database but it is configured to use memory based DAO by default. 
However it can be changed easily by setting the active profile as dao_cassandra in application.yml file.

Also, this project is using Ruby Cucumber framework to test application features as a BDD framework.   

### Working application example ###

Since it is free account in Heroku so it will require waking up time to run the application.

https://svg-converter.herokuapp.com/


### Compile project ###

This application is using a Gradle as a build system and it requires Java 8 JDK as a minimum requirement.

* Compiling UI project

The following gradle task will build and deploy it into Java backend project.

```
$ gradlew deployAngularDist
```

If you would like to compile only UI project, the following command can be used.
As this UI project is made of Angular framework, angular cli(7.0) should be installed to compile UI project.

```$xslt
# cd ns-svg-converter-angular
$ ng build
``` 

* Compiling backend application

```
$ gradlew clean build
```

### Running application ###

* Compiling application including UI and running application
```
$ ./gradlew clean build
$ ./gradlew deployAngularDist build
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
 :: Spring Boot ::        (v2.0.3.RELEASE)

```

* Browsing the application
```$xslt
http://localhost:8080/
```

### Creating docker container ###

Since ns-main-service project contains the Dockerfile, it can be deployed in Docker 
even it can be deployed in Kubernetes cluster.
 
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

```
$ gradlew bdd 
```

* BDD run result example
```
17 scenarios (17 passed)
135 steps (135 passed)
0m11.789s
```
