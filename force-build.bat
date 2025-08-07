@echo off
taskkill /f /im java.exe 2>nul
timeout /t 2 /nobreak >nul
rmdir /s /q build 2>nul
gradlew.bat build --no-daemon