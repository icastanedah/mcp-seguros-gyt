@echo off
echo Forzando limpieza...

REM Cerrar IntelliJ IDEA si está abierto
taskkill /f /im idea64.exe 2>nul
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul

REM Esperar un momento
timeout /t 3 /nobreak >nul

REM Forzar eliminación del directorio build
if exist build (
    echo Eliminando build...
    rmdir /s /q build 2>nul
    if exist build (
        echo Usando PowerShell para forzar eliminación...
        powershell -Command "Remove-Item -Path 'build' -Recurse -Force -ErrorAction SilentlyContinue"
    )
)

echo Build directory eliminado
gradlew build --no-daemon