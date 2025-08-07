@echo off
setlocal enabledelayedexpansion

echo 🚀 Iniciando MCP Security Analyzer (Modo Debug)...

REM Cambiar al directorio del script
cd /d "%~dp0"
echo 📍 Directorio actual: %CD%

REM Verificar que estamos en el directorio correcto
if not exist "build.gradle" (
    echo ❌ Error: No se encontró build.gradle en el directorio actual
    pause
    exit /b 1
)

REM Verificar Java
echo 🔍 Verificando Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java no encontrado en PATH
    echo 📥 Por favor instala Java 11+ desde: https://adoptium.net/
    pause
    exit /b 1
) else (
    echo ✅ Java encontrado
)

REM Verificar Node.js
echo 🔍 Verificando Node.js...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js no encontrado en PATH
    echo 📥 Por favor instala Node.js desde: https://nodejs.org/
    pause
    exit /b 1
) else (
    echo ✅ Node.js encontrado
)

REM Verificar que el JAR existe
if not exist "build\libs\mcp-graddle.jar" (
    echo 📦 Compilando proyecto...
    echo 🔍 Ejecutando: gradlew.bat build
    
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
    echo 🔍 Verificando contenido del directorio build\libs:
    if exist "build\libs" (
        dir build\libs
    ) else (
        echo ❌ El directorio build\libs no existe
    )
    pause
    exit /b 1
)

echo ✅ JAR encontrado: build\libs\mcp-graddle.jar

REM Verificar que el JAR es ejecutable
echo 🔍 Verificando que el JAR es ejecutable...
java -jar build\libs\mcp-graddle.jar --help >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  El JAR puede no ser ejecutable correctamente
    echo 🔍 Intentando ejecutar directamente...
    java -jar build\libs\mcp-graddle.jar
    pause
    exit /b 1
)

REM Iniciar el inspector MCP
echo 🌐 Iniciando inspector MCP...
echo 🔗 URL: http://localhost:6274/
echo ⏹️  Presiona Ctrl+C para detener
echo.

REM Usar ruta absoluta para el JAR
set JAR_PATH="%~dp0build\libs\mcp-graddle.jar"
echo 🔍 Ejecutando: npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH%

REM Ejecutar con más información de debug
npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH% 