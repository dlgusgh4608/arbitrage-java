#!/bin/bash
APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/arbitrageJava.jar"
LOG="/home/ubuntu/log/afterInstall.log"

NOW=$(date '+%Y-%m-%d %T')

echo "[$NOW] start afterInstall process" >> $LOG
if [ -f $JAR ]; then
  pkill -f "java -jar $JAR" || echo "[$NOW] Process not running" >> $LOG
else
  echo "[$NOW] Error: JAR file not found at $JAR"
  exit 1
fi

cd $APP_DIR

nohup java -jar $JAR &

echo "[$NOW] end afterInstall process" >> $LOG