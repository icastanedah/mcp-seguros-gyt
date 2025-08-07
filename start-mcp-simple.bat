@echo off
echo 🚀 Iniciando MCP Security Analyzer...

REM Cambiar al directorio del script
cd /d "%~dp0"

REM Compilar si es necesario
if not exist "build\libs\mcp-graddle.jar" (
    echo 📦 Compilando proyecto...
    gradlew.bat build
)

REM Iniciar el inspector MCP
echo 🌐 Iniciando inspector MCP...
echo 🔗 URL: http://localhost:6274/
echo.

npx @modelcontextprotocol/inspector@0.15.0 java -jar build\libs\mcp-graddle.jar 