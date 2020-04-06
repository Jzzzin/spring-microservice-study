#!/bin/sh
getPort() {
    echo $1 | cut -d : -f 3 | xargs basename
}

echo "********************************************************"
echo "Waiting for the eureka server to start on port $(getPort $EUREKASERVER_PORT)"
echo "********************************************************"
while ! `nc -z eurekaserver $(getPort $EUREKASERVER_PORT)`; do sleep 3; done
echo "******* Eureka Server has started"

echo "********************************************************"
echo "Waiting for the database server to start on port $(getPort $DATABASE_PORT)"
echo "********************************************************"
while ! `nc -z database $(getPort $DATABASE_PORT)`; do sleep 3; done
echo "******** Database Server has started "

echo "********************************************************"
echo "Waiting for the configuration server to start on port $(getPort $CONFIGSERVER_PORT)"
echo "********************************************************"
while ! `nc -z configserver $(getPort $CONFIGSERVER_PORT)`; do sleep 3; done
echo "*******  Configuration Server has started"

echo "********************************************************"
echo "Waiting for the zookeeper server to start on port $(getPort $ZOOKEEPER_PORT)"
echo "********************************************************"
while ! `nc -z zookeeper $(getPort $ZOOKEEPER_PORT)`; do sleep 10; done
echo "*******  zookeeper Server has started"

echo "********************************************************"
echo "Waiting for the REDIS server to start  on port $(getPort $REDIS_PORT)"
echo "********************************************************"
while ! `nc -z redis $(getPort $REDIS_PORT)`; do sleep 10; done
echo "******* REDIS has started"

echo "********************************************************"
echo "Waiting for the ZIPKIN server to start  on port $ZIPKIN_PORT"
echo "********************************************************"
while ! `nc -z zipkin $ZIPKIN_PORT`; do sleep 10; done
echo "******* ZIPKIN has started"

echo "********************************************************"
echo "Starting License Server with Configuration Service via Eureka : $EUREKASERVER_URI:$SERVER_PORT"
echo "Using Kafka Server: $KAFKASERVER_URI"
echo "Using ZK    Server: $ZKSERVER_URI"
echo "USing Profile: $PROFILE"
echo "License service will use $AUTHSERVER_URI for URI"
echo "********************************************************"
java -Djava.security.egd=file:/dev/./urandom -Dserver.port=$SERVER_PORT   \
     -Deureka.client.serviceUrl.defaultZone=$EUREKASERVER_URI             \
     -Dspring.cloud.config.uri=$CONFIGSERVER_URI                          \
     -Dspring.cloud.stream.kafka.binder.zkNodes=$ZKSERVER_URI             \
     -Dspring.cloud.stream.kafka.binder.brokers=$KAFKASERVER_URI          \
     -Dspring.zipkin.baseUrl=$ZIPKIN_URI                                  \
     -Dsecurity.oauth2.resource.userInfoUri=$AUTHSERVER_URI               \
     -Dspring.profiles.active=$PROFILE -jar /usr/local/licensingservice/@project.build.finalName@.jar
