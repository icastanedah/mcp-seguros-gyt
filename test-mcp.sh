#!/bin/bash
cd "c:/Users/icastaneda/Documents/hackaton/mcp-graddle"

echo "Compilando..."
./gradlew build -q

echo ""
echo "Iniciando servidor MCP..."
echo "Enviando comandos de prueba..."
echo ""

{
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
echo '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"scan_repo","arguments":{"repo_path":"C:\\Users\\icastaneda\\Documents\\walli-new-team\\policy-management"}}}'
} | java -cp "build/libs/*" org.example.mcp.MCPServer

echo ""
echo "Presiona Enter para salir..."
read