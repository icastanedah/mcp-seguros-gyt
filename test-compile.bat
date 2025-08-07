@echo off
echo === PRUEBA DE COMPILACIÓN ===

REM Crear directorio de salida
if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

echo.
echo 1. Compilando clases del modelo...
javac -d build\classes src\main\java\org\example\mcp\model\*.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error compilando modelo
    goto :error
)

echo.
echo 2. Compilando SecurityAnalyzer...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\SecurityAnalyzer.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error compilando SecurityAnalyzer
    goto :error
)

echo.
echo 3. Compilando MCPServerSimple...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\MCPServerSimple.java
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error compilando MCPServerSimple
    goto :error
)

echo.
echo 4. Ejecutando prueba...
java -cp build\classes org.example.mcp.MCPServerSimple

echo.
echo ✅ COMPILACIÓN Y EJECUCIÓN EXITOSA
goto :end

:error
echo.
echo ❌ FALLÓ LA COMPILACIÓN
pause
exit /b 1

:end
pause