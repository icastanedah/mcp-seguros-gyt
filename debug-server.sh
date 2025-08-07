#!/bin/bash
cd "$(dirname "$0")"

# Compilar
./gradlew compileJava -x test -q >/dev/null 2>&1

# Ejecutar con debug
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | java -cp "build/libs/*" org.example.mcp.MCPServer