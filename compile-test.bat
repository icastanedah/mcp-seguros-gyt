@echo off
echo === PRUEBA DE COMPILACIÓN ===

if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

echo 1. Compilando modelo...
javac -d build\classes src\main\java\org\example\mcp\model\*.java

echo 2. Compilando SecurityAnalyzer...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\SecurityAnalyzer.java

echo 3. Compilando MCPServerSimple...
javac -cp build\classes -d build\classes src\main\java\org\example\mcp\MCPServerSimple.java

echo 4. Ejecutando prueba...
java -cp build\classes org.example.mcp.MCPServerSimple

pause