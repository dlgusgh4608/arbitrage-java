#!/bin/bash
APP_DIR="/home/ubuntu/app"
JAR_NAME="arbitrage-java.jar"

echo "Starting application..."
if [ -f "$APP_DIR/$JAR_NAME" ]; then
  nohup java -jar "$APP_DIR/$JAR_NAME" &
else
  echo "Error: JAR file not found at $APP_DIR/$JAR_NAME"
  exit 1
fi