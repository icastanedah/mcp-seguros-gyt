@echo off
echo Cerrando procesos Java...
taskkill /f /im java.exe 2>nul

echo Eliminando build directory...
rmdir /s /q build 2>nul

echo Compilando con Gradle...
gradlew.bat build

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilación exitosa
    echo Iniciando MCP Server...
    java -jar build\libs\mcp-graddle.jar
) else (
    echo ❌ Error de compilación
    pause
)