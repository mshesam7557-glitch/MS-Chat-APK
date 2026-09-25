@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "GRADLE_VERSION=8.7"
set "GRADLE_DIR=%USERPROFILE%\.gradle\mschat-wrapper\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%GRADLE_DIR%\bin\gradle.bat"

if exist "%GRADLE_BIN%" goto run_gradle

set "TMP_ZIP=%TEMP%\mschat-gradle-%GRADLE_VERSION%.zip"
set "TMP_DIR=%TEMP%\mschat-gradle-extract-%GRADLE_VERSION%"
if exist "%TMP_ZIP%" del /q "%TMP_ZIP%" >nul 2>&1
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%" >nul 2>&1

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$urls=@('https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip','https://downloads.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip');" ^
  "$ok=$false; foreach($u in $urls){ try { Invoke-WebRequest -UseBasicParsing -Uri $u -OutFile '%TMP_ZIP%'; $ok=$true; break } catch { } }; if(-not $ok){ throw 'دانلود Gradle %GRADLE_VERSION% ناموفق بود.' }"
if errorlevel 1 exit /b %errorlevel%

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$dest='%USERPROFILE%\.gradle\mschat-wrapper';" ^
  "New-Item -ItemType Directory -Force -Path $dest | Out-Null;" ^
  "Expand-Archive -LiteralPath '%TMP_ZIP%' -DestinationPath $dest -Force"
if errorlevel 1 exit /b %errorlevel%

del /q "%TMP_ZIP%" >nul 2>&1
rmdir /s /q "%TMP_DIR%" >nul 2>&1

:run_gradle
if not exist "%GRADLE_BIN%" (
  echo Gradle installation not found: %GRADLE_BIN%
  exit /b 1
)
call "%GRADLE_BIN%" %*
exit /b %errorlevel%
