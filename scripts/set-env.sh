#!/bin/bash
ENV_POINT="/home/ubuntu/.env"
APP_DIR="/home/ubuntu/app"

if [ -f "$ENV_POINT" ]; then
    cp $ENV_POINT $APP_DIR
else
    echo "env파일이 없습니다."
fi