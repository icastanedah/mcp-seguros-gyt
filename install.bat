@echo off
echo 🚀 Instalador Universal para MCP Security Analyzer
echo ==================================================

REM Verificar si Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java no encontrado
    echo 📥 Por favor instala Java 11 o superior desde:
    echo    https://adoptium.net/
    echo.
    echo 🔗 O usa Chocolatey:
    echo    choco install openjdk11
    pause
    exit /b 1
) else (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        echo ✅ Java encontrado: %%g
    )
)

REM Verificar si Node.js está instalado
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js no encontrado
    echo 📥 Por favor instala Node.js 16 o superior desde:
    echo    https://nodejs.org/
    echo.
    echo 🔗 O usa Chocolatey:
    echo    choco install nodejs
    pause
    exit /b 1
) else (
    for /f "tokens=1" %%n in ('node --version') do (
        echo ✅ Node.js encontrado: %%n
    )
)

REM Compilar el proyecto
echo 🔨 Compilando proyecto...
gradlew.bat clean build --quiet
if %errorlevel% neq 0 (
    echo ❌ Error en la compilación
    pause
    exit /b 1
)

echo ✅ Compilación exitosa!
echo.
echo 🎉 ¡Instalación completada!
echo ==========================
echo.
echo 📋 Para usar el MCP Security Analyzer:
echo    1. Ejecuta: start-mcp.bat
echo    2. Abre tu navegador en: http://localhost:6274/
echo    3. Usa las herramientas disponibles:
echo       - scan_repo: Analiza repositorios (usa 'auto' para búsqueda automática)
echo       - analyze_policies: Analiza políticas de desarrollo
echo.
echo 🔧 Comandos útiles:
echo    - start-mcp.bat        # Iniciar el inspector
echo    - gradlew.bat build    # Recompilar el proyecto
echo    - gradlew.bat clean    # Limpiar build
echo.
echo 📚 Documentación: README.md
echo.
pause 