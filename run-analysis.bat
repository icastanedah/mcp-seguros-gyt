@echo off
echo ========================================
echo    MCP INSPECTOR - ANALISIS DE POLITICAS
echo ========================================
echo.

cd /d "c:\Users\icastaneda\Documents\hackaton\mcp-graddle"

echo 📦 Compilando proyecto...
call gradlew build -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error en la compilación
    pause
    exit /b 1
)

echo ✅ Compilación exitosa
echo.

echo 🚀 Ejecutando análisis de políticas...
echo.
call gradlew run -q --args="TestSecurity"

echo.
echo 📊 Análisis completado. Revise los reportes generados:
echo    • mcp-analysis-report.html (Principal)
echo    • mcp-analysis-report.json (Datos)
echo    • policy-analysis-report.html (Respaldo)
echo.

pause