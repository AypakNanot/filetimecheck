@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 正在启动文件时间校验工具...
java -cp "target\filetimecheck-1.0-SNAPSHOT.jar;target\libs\*" com.aypak.filetimecheck.HelloApplication
pause
