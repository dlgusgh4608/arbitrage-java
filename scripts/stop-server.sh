#!/bin/bash
APP_DIR="/home/ubuntu/app"
JAR="$APP_DIR/arbitrageJava.jar"
LOG="/home/ubuntu/log/stop_process.log"

NOW=$(date '+%Y-%m-%d %T')

echo "[$NOW] stop_server process start" >> $LOG
if [ -f $JAR ]; then
  pkill -f "java -jar $JAR" || echo "[$NOW] Process not running" >> $LOG
else
  echo "[$NOW] Error: JAR file not found at $JAR"
  exit 1
fi
echo "[$NOW] stop_server process end" >> $LOG