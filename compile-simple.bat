@echo off
echo === COMPILACIÓN SIMPLE SIN JACKSON ===

if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

echo 1. Compilando SimpleTool...
javac -d build\classes src\main\java\org\example\mcp\model\SimpleTool.java
if %ERRORLEVEL% NEQ 0 goto error

echo 2. Compilando SecurityAnalyzer...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\SecurityAnalyzer.java
if %ERRORLEVEL% NEQ 0 goto error

echo 3. Compilando MCPServerSimple...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\MCPServerSimple.java
if %ERRORLEVEL% NEQ 0 goto error

echo 4. Ejecutando...
java -cp build\classes org.example.mcp.MCPServerSimple

echo ✅ ÉXITO
goto end

:error
echo ❌ ERROR DE COMPILACIÓN

:end
pause