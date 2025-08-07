@echo off
cd /d "%~dp0"

REM Compilar si es necesario (silencioso)
if not exist "build\libs\mcp-graddle.jar" (
    gradlew jar -x test -q >nul 2>&1
)

REM Ejecutar servidor MCP simple
java -cp "build\libs\*" org.example.mcp.SimpleMCPServer 2>nul