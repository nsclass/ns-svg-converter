#!/bin/sh

set -e

echo ${SERVICE_NAME}
echo ${SERVICE_VERSION}

DIR="/opt/ns/svg-service"
if [ -z "$DIR" ]; then
    # error; for some reason, the path is not accessible
    # to the script (e.g. permissions re-evaled after suid)
    exit 1 # fail
fi

if [ ! -f $DIR/keystore.jks ]
then
    echo Creating secure keystore...
    keytool -genkey -alias ${SERVICE_NAME} -keyalg RSA -keystore $DIR/keystore.jks -keysize 2048 -dname "CN=NSEO ${SERVICE_NAME}-service, OU=NSEO, O=NSEO, L=ICN, ST=SE, C=KR" -keypass pleasedonotuse -storepass pleasedonotuse ;
    keytool -export -alias ${SERVICE_NAME} -file $DIR/${SERVICE_NAME}_default.crt -keystore  $DIR/keystore.jks -keypass pleasedonotuse -storepass pleasedonotuse ;
    keytool -import -noprompt -trustcacerts -alias ${SERVICE_NAME} -keypass pleasedonotuse -file $DIR/${SERVICE_NAME}_default.crt -keystore $DIR/truststore.jks -storepass pleasedonotuse
fi

TZ="UTC"
export TZ

JVM_BASE_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:MaxGCPauseMillis=10"

JVM_OPTS="-Xmx${JVM_MAX_HEAP} -Xms${JVM_MIN_HEAP} ${JVM_BASE_OPTS}"

START_CMD="java -jar -server ${JVM_OPTS} ${DIR}/${SERVICE_NAME}-${SERVICE_VERSION}.jar"

eval ${START_CMD}

exit 0