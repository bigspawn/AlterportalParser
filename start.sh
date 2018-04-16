#!/usr/bin/bash

HOME_PATH=/home/root/Applications/AlterportalParser
LIB_DIR=$HOME_PATH/lib

echo $HOME_PATH
echo $LIB_DIR

export CLASSPATH="$HOME_PATH/*:$LIB_DIR/*:$HOME_PATH/settings.ini"
echo $CLASSPATH

java \
    -Dsettings.path=$HOME_PATH/settings.ini \
    -Dlog4j.configurationFile=$HOME_PATH/log4j2.xml \
    -server \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
    -Xms128m \
    -Xmx258m \
    -XX:+PrintGCDetails \
    -XX:+PrintGCTimeStamps \
    -XX:+UseGCLogFileRotation \
    -XX:NumberOfGCLogFiles=5 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=$HOME_PATH/dumps \
    -XX:GCLogFileSize=2M \
    -Xloggc:$HOME_PATH/gc/gc.log \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=12345 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    #    -Djava.rmi.server.hostname=192.168.31.158 \
    -cp $CLASSPATH ru.bigspawn.parser.Main