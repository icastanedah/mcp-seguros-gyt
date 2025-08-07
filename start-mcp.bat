@echo off
echo 🚀 Iniciando MCP Security Analyzer...

REM Verificar que el JAR existe
if not exist "build\libs\mcp-graddle.jar" (
    echo 📦 Compilando proyecto...
    gradlew.bat build --quiet
)

REM Iniciar el inspector MCP
echo 🌐 Iniciando inspector MCP...
echo 🔗 URL: http://localhost:6274/
echo ⏹️  Presiona Ctrl+C para detener
echo.

npx @modelcontextprotocol/inspector@0.15.0 java -jar build\libs\mcp-graddle.jar 