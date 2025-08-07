@echo off
echo Eliminando archivos innecesarios...

del /q src\main\java\org\example\Main.java 2>nul
del /q src\main\java\org\example\MCPClient.java 2>nul
del /q src\main\java\org\example\TestSecurity.java 2>nul
del /q src\main\java\org\example\mcp\ChecklistAuditor.java 2>nul
del /q src\main\java\org\example\mcp\MCPServerSimple.java 2>nul
del /q src\main\java\org\example\mcp\SimpleMCPServer.java 2>nul
del /q src\main\java\org\example\mcp\SimpleMCPServerTest.java 2>nul
del /q src\main\java\org\example\mcp\TaskManager.java 2>nul
del /q src\main\java\org\example\mcp\model\SimpleTool.java 2>nul

echo Archivos eliminados
pause