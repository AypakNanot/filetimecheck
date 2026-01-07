@echo off
chcp 65001 >nul
echo ====================================
echo   WiX 3.11 下载工具
echo ====================================
echo.
echo 正在下载 WiX 3.11 二进制工具包...
echo.

set DOWNLOAD_URL=https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip
set OUTPUT_DIR=C:\wix
set OUTPUT_FILE=%TEMP%\wix311-binaries.zip

echo 下载链接: %DOWNLOAD_URL%
echo 输出目录: %OUTPUT_DIR%
echo.

:: 创建输出目录
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

:: 使用 PowerShell 下载
echo 正在下载...
powershell -Command "& {Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%OUTPUT_FILE%'}"

if errorlevel 1 (
    echo.
    echo [错误] 下载失败
    echo.
    echo 请手动下载:
    echo 1. 访问: https://github.com/wixtoolset/wix3/releases
    echo 2. 下载: wix311-binaries.zip
    echo 3. 解压到: C:\wix
    pause
    exit /b 1
)

echo.
echo 正在解压...
powershell -Command "& {Expand-Archive -Path '%OUTPUT_FILE%' -DestinationPath '%OUTPUT_DIR%' -Force}"

if errorlevel 1 (
    echo.
    echo [错误] 解压失败
    echo 请手动解压 %OUTPUT_FILE% 到 %OUTPUT_DIR%
    pause
    exit /b 1
)

:: 清理临时文件
del "%OUTPUT_FILE%"

echo.
echo ====================================
echo   下载完成！
echo ====================================
echo.
echo WiX 已安装到: %OUTPUT_DIR%
echo.
echo 请将以下路径添加到系统 PATH:
echo %OUTPUT_DIR%\bin
echo.
echo 添加 PATH 后，重启命令行窗口，然后运行打包命令。
echo.

:: 验证安装
if exist "%OUTPUT_DIR%\bin\candle.exe" (
    echo [验证] candle.exe 存在 ✓
) else (
    echo [验证] candle.exe 不存在 ✗
)

if exist "%OUTPUT_DIR%\bin\light.exe" (
    echo [验证] light.exe 存在 ✓
) else (
    echo [验证] light.exe 不存在 ✗
)

echo.
pause
