@echo off
echo === COMPILANDO Y EJECUTANDO MCP SERVER ===

if not exist "build" md build
if not exist "build\classes" md build\classes

echo 1. Compilando modelo...
javac -d build\classes src\main\java\org\example\mcp\model\*.java
if %ERRORLEVEL% NEQ 0 goto error

echo 2. Compilando SecurityAnalyzer...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\SecurityAnalyzer.java
if %ERRORLEVEL% NEQ 0 goto error

echo 3. Compilando MCPServer...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\MCPServer.java
if %ERRORLEVEL% NEQ 0 goto error

echo 4. Iniciando MCP Server...
echo MCP Server iniciado - Esperando conexiones JSON-RPC...
java -cp build\classes org.example.mcp.MCPServer

goto end

:error
echo ❌ ERROR DE COMPILACIÓN
pause
exit /b 1

:end