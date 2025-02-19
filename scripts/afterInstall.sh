#!/bin/bash

APP_DIR="/home/ubuntu/app"

JAR="$APP_DIR/arbitrageJava.jar"
LOG="/home/ubuntu/log/afterInstall.log"

NOW=$(date '+%Y-%m-%d %T')

JVM_OPTS=(
  "-Xmx192M" #최대값
  "-Xms64M" #초기값
  "-XX:MaxMetaspaceSize=192M" #최대값
  "-XX:MetaspaceSize=128M" #초기값
)

# t2.micro기준 작성했습니다. 좋은 서버 쓰려면 값 바꾸세용

if pgrep -f "java.*arbitrageJava.jar"; then
    echo "[$NOW] try process shutdown" >> "$LOG"
    pkill -f "java.*arbitrageJava.jar" || echo "[$NOW] Process shutdown failed" >> "$LOG"
    sleep 5

    if pgrep -f "java.*arbitrageJava.jar"; then
        echo "[$NOW] try process shutdown now(SIGKILL)" >> "$LOG"
        pkill -9 -f "java.*arbitrageJava.jar" || echo "[$NOW] Process shutdown now failed" >> "$LOG"
        sleep 2
    fi
else
    echo "[$NOW] Process not running" >> "$LOG"
fi

cd $APP_DIR

nohup java "${JVM_OPTS[@]}" -jar "$JAR" > /dev/null 2>&1 &

echo "[$NOW] end afterInstall process" >> $LOG
exit 0