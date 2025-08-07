#!/bin/bash

echo "========================================="
echo "   MCP INSPECTOR - ANÁLISIS DE POLÍTICAS"
echo "========================================="
echo

cd "$(dirname "$0")"

echo "📦 Compilando clases Java..."

# Crear directorio de clases compiladas
mkdir -p build/classes

# Compilar todas las clases Java
find src/main/java -name "*.java" -exec javac -d build/classes -cp "src/main/java" {} +

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
    echo
    echo "🚀 Ejecutando análisis..."
    echo
    
    # Ejecutar la clase principal
    java -cp build/classes org.example.TestSecurity
    
    echo
    echo "📊 Análisis completado!"
    echo "Revise los reportes en: C:\\Users\\icastaneda\\Documents\\hackaton\\"
else
    echo "❌ Error en la compilación"
    exit 1
fi