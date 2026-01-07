@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 正在启动文件时间校验工具...
java -jar target\filetimecheck-1.0-SNAPSHOT.jar
pause
