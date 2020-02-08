
# jlog

jlog helps you to find exceptions through log files. 

It helps to gather only java exceptions needed to troubleshoot your app in microservice architectures and production environment.

jlog can gather and analyze log files from a remote server or local computer.

## Quick-start

The quickest way to get started is to clone this repository. 
jlog is a [Spring Boot](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) application, packaged as an executable jar.
Note that the jlog requires minimum JRE 8. For example:

Before running the jlog server, make sure you have elasticsearch installed.  By default, jlog stores data in elasticsearch.

Once the server is running, you can gather and analyze log files with the jlog api at `http://your_host:8181/api/v1/jlog?readerType=REMOTE`.


### jlog configuration

Here's an example configuration, you can change it in application.properties, however, you can setup jlog by sending arguments to the app.

```
jlog.ip=127.0.0.1
jlog.port=22
jlog.username=
jlog.password=
jlog.timeout=3000
jlog.elastic.ip=127.0.0.1
jlog.elastic.port=9200
jlog.logdir=/opt/logs
```

```
$ java -jar ./target/jlog-0.0.1-SNAPSHOT.jar --h 127.0.0.1 --p 22 --u root --pas 123 --logdir /opt
```


### Elasticsearch

The Elasticsearch component uses Elasticsearch 7+ features.



## Running the server from source

```
# Build the server 
$ mvn clean install -DskipTests=true 
# Run the server
$ java -jar ./target/jlog-0.0.1-SNAPSHOT.jar
```

