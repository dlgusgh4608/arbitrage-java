#!/bin/bash
APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/arbitrageJava.jar"
LOG="/home/ubuntu/log/start_process.log"

NOW=$(date '+%Y-%m-%d %T')


echo "[$NOW] start_server process start" >> $LOG
if [ -f $JAR ]; then
  nohup java -jar $JAR &
else
  echo "[$NOW] Error: JAR file not found at $JAR"
  exit 1
fi
echo "[$NOW] start_server process end" >> $LOG