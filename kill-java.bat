@echo off
echo Cerrando procesos Java...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
timeout /t 3 /nobreak >nul

echo Eliminando directorio build...
if exist build (
    attrib -r -h -s build\*.* /s /d
    rmdir /s /q build
)

echo Listo para compilar
pause