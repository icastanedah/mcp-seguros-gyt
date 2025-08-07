@echo off
setlocal enabledelayedexpansion

echo 🚀 Iniciando MCP Security Analyzer para Windows...

REM Cambiar al directorio del script
cd /d "%~dp0"

REM Verificar que estamos en el directorio correcto
if not exist "build.gradle" (
    echo ❌ Error: No se encontró build.gradle en el directorio actual
    echo 📍 Directorio actual: %CD%
    pause
    exit /b 1
)

REM Verificar que el JAR existe
if not exist "build\libs\mcp-graddle.jar" (
    echo 📦 Compilando proyecto...
    gradlew.bat build
    if !errorlevel! neq 0 (
        echo ❌ Error en la compilación
        pause
        exit /b 1
    )
)

REM Verificar que el JAR existe después de la compilación
if not exist "build\libs\mcp-graddle.jar" (
    echo ❌ No se pudo encontrar el archivo JAR: build\libs\mcp-graddle.jar
    pause
    exit /b 1
)

echo ✅ JAR encontrado: build\libs\mcp-graddle.jar

REM Limpiar procesos anteriores
echo 🔄 Limpiando procesos anteriores...
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im node.exe >nul 2>&1

REM Esperar un momento
timeout /t 2 /nobreak >nul

REM Iniciar el inspector MCP con configuración específica para Windows
echo 🌐 Iniciando inspector MCP...
echo 🔗 URL: http://localhost:6274/
echo ⏹️  Presiona Ctrl+C para detener
echo.

REM Usar ruta absoluta y configuración específica para Windows
set JAR_PATH="%~dp0build\libs\mcp-graddle.jar"
echo 🔍 Ejecutando: npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH%

REM Ejecutar con configuración específica para Windows
set NODE_OPTIONS=--max-old-space-size=4096
npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH% 