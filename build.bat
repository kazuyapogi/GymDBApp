@echo off
REM Compiles the app into the out folder.
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out -cp "lib\*" src\gym\*.java src\gym\model\*.java src\gym\data\*.java src\gym\logic\*.java src\gym\ui\*.java
if errorlevel 1 (
  echo.
  echo Build failed. Check that the JDK is installed and javac is on the PATH.
  pause
  exit /b 1
)
echo Build finished. Run run.bat to start the app.
