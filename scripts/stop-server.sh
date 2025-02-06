#!/bin/bash
APP_DIR="/home/ubuntu/app"
JAR_NAME="arbitrage-java.jar"

echo "Stopping application..."
if [ -f "$APP_DIR/$JAR_NAME" ]; then
  pkill -f "java -jar $APP_DIR/$JAR_NAME" || echo "Process not running"
else
  echo "Error: JAR file not found at $APP_DIR/$JAR_NAME"
  exit 1
fi