#!/bin/bash

function _start {
JAVA_ARGS="$*"
java -version > pipeline.log 2>&1

java -server -Xms32m -Xmx32m -verbose:gc $JAVA_ARGS -Dcom.sun.management.jmxremote.port=9009 \
   -Dcom.sun.management.jmxremote.authenticate=false\
   -Dcom.sun.management.jmxremote.ssl=false \
   -jar jmstoolkit-pipeline.jar >>pipeline.log 2>&1 &

echo $! >pipeline.pid
}

case "$1" in
'start')
   shift
   _start $*
;;
'stop')
  kill -9 `cat pipeline.pid`
  rm pipeline.pid
;;
*)
  echo "Usage `basename $0` ( start | stop )"
;;
esac
