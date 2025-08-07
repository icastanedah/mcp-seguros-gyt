@echo off
echo Compilando proyecto MCP...

REM Crear directorio de clases
if not exist "build\classes" mkdir build\classes

REM Compilar todas las clases Java
javac -cp "lib\*" -d build\classes src\main\java\org\example\mcp\model\*.java src\main\java\org\example\mcp\*.java src\main\java\org\example\*.java

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilación exitosa
) else (
    echo ❌ Error de compilación
)

pause