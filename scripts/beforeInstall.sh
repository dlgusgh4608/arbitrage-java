#!/bin/bash
APP_DIR="/home/ubuntu/app"
LOG_DIR="/home/ubuntu/log"
ENV_POINT="/home/ubuntu/.env"
LOG="/home/ubuntu/log/beforeInstall.log"

NOW=$(date '+%Y-%m-%d %T')

echo "[$NOW] start beforeInstall script" >> $LOG

rm -rf $APP_DIR

mkdir -p $APP_DIR
mkdir -p $LOG_DIR

if [ -f "$ENV_POINT" ]; then
    cp $ENV_POINT $APP_DIR
else
    echo "[$NOW] .env file is not found in root" >> $LOG
    exit 1
fi

echo "[$NOW] end beforeInstall script" >> $LOG