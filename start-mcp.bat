@echo off
setlocal enabledelayedexpansion

echo 🚀 Iniciando MCP Security Analyzer...

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
    echo 🔍 Ejecutando: gradlew.bat build --quiet
    
    gradlew.bat build --quiet
    if !errorlevel! neq 0 (
        echo ❌ Error en la compilación
        echo 🔍 Verificando si gradlew.bat existe:
        if exist "gradlew.bat" (
            echo ✅ gradlew.bat encontrado
        ) else (
            echo ❌ gradlew.bat no encontrado
        )
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
        echo 🔍 Verificando directorio build:
        if exist "build" (
            dir build
        ) else (
            echo ❌ El directorio build no existe
        )
    )
    pause
    exit /b 1
)

echo ✅ JAR encontrado: build\libs\mcp-graddle.jar

REM Iniciar el inspector MCP
echo 🌐 Iniciando inspector MCP...
echo 🔗 URL: http://localhost:6274/
echo ⏹️  Presiona Ctrl+C para detener
echo.

REM Usar ruta absoluta para el JAR
set JAR_PATH="%~dp0build\libs\mcp-graddle.jar"
echo 🔍 Ejecutando: npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH%

npx @modelcontextprotocol/inspector@0.15.0 java -jar %JAR_PATH% 