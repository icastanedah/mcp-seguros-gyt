#!/bin/bash

echo "🚀 Iniciando MCP Security Analyzer..."

# Configurar Java
if [ "$(uname -s)" = "Darwin" ]; then
    export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
    export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
fi

# Verificar que el JAR existe
if [ ! -f "build/libs/mcp-graddle.jar" ]; then
    echo "📦 Compilando proyecto..."
    ./gradlew build --quiet
fi

# Iniciar el inspector MCP
echo "🌐 Iniciando inspector MCP..."
echo "🔗 URL: http://localhost:6274/"
echo "⏹️  Presiona Ctrl+C para detener"
echo ""

npx @modelcontextprotocol/inspector@0.15.0 java -jar build/libs/mcp-graddle.jar
