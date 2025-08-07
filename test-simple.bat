@echo off
cd /d "c:\Users\icastaneda\Documents\hackaton\mcp-graddle"
echo Compilando...
call gradlew build -q
if errorlevel 1 (
    echo Error en compilacion
    pause
    exit /b 1
)
echo Ejecutando servidor MCP...
echo.
type test-security.json | java -cp "build\libs\*;build\classes\java\main" org.example.mcp.MCPServer
pause