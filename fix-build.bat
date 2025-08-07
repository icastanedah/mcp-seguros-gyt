@echo off
echo Renombrando build directory...
if exist build (
    ren build build_old
)

echo Compilando...
gradlew.bat build

echo Listo. JAR en build\libs\mcp-graddle.jar