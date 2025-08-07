@echo off
cd /d "%~dp0"
npx @modelcontextprotocol/inspector java -cp "build\libs\*" org.example.mcp.SimpleMCPServer
pause