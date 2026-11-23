@echo off
REM Starts the app. Run build.bat first.
cd /d "%~dp0"
java -cp "out;lib\*" gym.Main
if errorlevel 1 pause
