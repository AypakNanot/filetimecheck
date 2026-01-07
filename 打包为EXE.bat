@echo off
chcp 65001 >nul
echo ====================================
echo   文件时间校验工具 - EXE 打包工具
echo ====================================
echo.
echo 请选择打包方式:
echo.
echo [1] 使用 jpackage 打包 (需要安装 WiX Toolset)
echo [2] 使用在线打包服务 (推荐 - 无需安装工具)
echo [3] 创建带图标的 EXE (使用 jpackage + 自定义图标)
echo [0] 退出
echo.
set /p choice="请输入选项 (0-3): "

if "%choice%"=="1" goto jpackage
if "%choice%"=="2" goto online
if "%choice%"=="3" goto custom
if "%choice%"=="0" goto end
goto invalid

:jpackage
echo.
echo 正在检查 jpackage...
jpackage --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 jpackage 工具
    echo 请确保使用 JDK 16 或更高版本
    pause
    goto end
)
echo.
echo 正在使用 jpackage 创建 EXE...
jpackage ^
  --input target ^
  --name 文件时间校验工具 ^
  --main-jar filetimecheck-1.0-SNAPSHOT.jar ^
  --main-class com.aypak.filetimecheck.HelloApplication ^
  --type exe ^
  --dest target/dist ^
  --app-version 1.0.0 ^
  --vendor aypak ^
  --win-menu ^
  --win-shortcut
if errorlevel 1 (
    echo.
    echo [失败] 打包失败
    echo.
    echo 可能原因:
    echo 1. 未安装 WiX Toolset 3.0+
    echo    请从 https://wixtoolset.org/ 下载并安装
    echo 2. WiX 未添加到 PATH 环境变量
    echo.
    echo 或者选择选项 [2] 使用在线打包服务
) else (
    echo.
    echo [成功] EXE 文件已创建: target/dist\文件时间校验工具-1.0.0.exe
)
pause
goto end

:online
echo.
echo ====================================
echo   在线打包服务推荐
echo ====================================
echo.
echo 由于本地打包需要安装 WiX 工具，推荐使用以下在线服务:
echo.
echo 1. Jar2Exe (免费)
echo    网址: https://www.jar2exe.com/
echo    上传 target\filetimecheck-1.0-SNAPSHOT.jar 即可
echo.
echo 2. Launch4j Online
echo    网址: http://launch4j.sourceforge.net/
echo    下载 launch4j 后使用图形界面创建 EXE
echo.
echo 3. exe4j (付费，有试用期)
echo    网址: https://www.ej-technologies.com/products/exe4j
echo.
echo 4. JWrapper (免费)
echo    网址: https://www.jwrapper.com/
echo.
pause
goto end

:custom
echo.
echo 使用自定义图标打包...
echo.
set /p iconpath="请输入图标文件路径 (留空跳过): "
if "%iconpath%"=="" goto noicon
if not exist "%iconpath%" (
    echo [错误] 找不到图标文件: %iconpath%
    pause
    goto end
)
echo.
echo 正在使用自定义图标创建 EXE...
jpackage ^
  --input target ^
  --name 文件时间校验工具 ^
  --main-jar filetimecheck-1.0-SNAPSHOT.jar ^
  --main-class com.aypak.filetimecheck.HelloApplication ^
  --type exe ^
  --dest target/dist ^
  --app-version 1.0.0 ^
  --vendor aypak ^
  --icon "%iconpath%" ^
  --win-menu ^
  --win-shortcut
if errorlevel 1 (
    echo [失败] 打包失败
) else (
    echo [成功] EXE 文件已创建
)
pause
goto end

:noicon
echo.
echo 正在创建 EXE (无自定义图标)...
jpackage ^
  --input target ^
  --name 文件时间校验工具 ^
  --main-jar filetimecheck-1.0-SNAPSHOT.jar ^
  --main-class com.aypak.filetimecheck.HelloApplication ^
  --type exe ^
  --dest target/dist ^
  --app-version 1.0.0 ^
  --vendor aypak ^
  --win-menu ^
  --win-shortcut
if errorlevel 1 (
    echo [失败] 打包失败
) else (
    echo [成功] EXE 文件已创建
)
pause
goto end

:invalid
echo.
echo [错误] 无效的选项
pause

:end
