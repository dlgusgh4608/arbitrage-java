#!/bin/bash
ENV_POINT="/home/ubuntu/.env"
APP_DIR="/home/ubuntu/app"
LOG="/home/ubuntu/log/set_env_process.log"

NOW=$(date '+%Y-%m-%d %T')


if [ -f "$ENV_POINT" ]; then
    cp $ENV_POINT $APP_DIR
else
    echo "[$NOW] .env file is not found in root" >> $LOG
fi