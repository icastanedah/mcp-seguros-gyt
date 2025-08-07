#!/bin/bash

echo "🚀 Instalador Universal para MCP Security Analyzer"
echo "=================================================="

# Detectar el sistema operativo
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    CYGWIN*)    MACHINE=Cygwin;;
    MINGW*)     MACHINE=MinGw;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

echo "📋 Sistema detectado: $MACHINE"

# Verificar si Java está instalado
check_java() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo "✅ Java encontrado: $JAVA_VERSION"
        return 0
    else
        echo "❌ Java no encontrado"
        return 1
    fi
}

# Instalar Java en macOS
install_java_mac() {
    echo "🍎 Instalando Java en macOS..."
    if command -v brew &> /dev/null; then
        echo "📦 Usando Homebrew..."
        brew install openjdk@11
        echo "🔗 Configurando Java..."
        echo 'export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
        echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@11"' >> ~/.zshrc
        source ~/.zshrc
    else
        echo "❌ Homebrew no encontrado. Por favor instala Homebrew primero:"
        echo "   /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        exit 1
    fi
}

# Instalar Java en Linux
install_java_linux() {
    echo "🐧 Instalando Java en Linux..."
    if command -v apt-get &> /dev/null; then
        echo "📦 Usando apt-get..."
        sudo apt-get update
        sudo apt-get install -y openjdk-11-jdk
    elif command -v yum &> /dev/null; then
        echo "📦 Usando yum..."
        sudo yum install -y java-11-openjdk-devel
    elif command -v dnf &> /dev/null; then
        echo "📦 Usando dnf..."
        sudo dnf install -y java-11-openjdk-devel
    else
        echo "❌ Gestor de paquetes no soportado. Por favor instala Java 11 manualmente."
        exit 1
    fi
}

# Verificar si Node.js está instalado
check_node() {
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node --version)
        echo "✅ Node.js encontrado: $NODE_VERSION"
        return 0
    else
        echo "❌ Node.js no encontrado"
        return 1
    fi
}

# Instalar Node.js
install_node() {
    echo "📦 Instalando Node.js..."
    if command -v curl &> /dev/null; then
        curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
        sudo apt-get install -y nodejs
    else
        echo "❌ curl no encontrado. Por favor instala Node.js manualmente desde:"
        echo "   https://nodejs.org/"
        exit 1
    fi
}

# Verificar dependencias
echo "🔍 Verificando dependencias..."

# Verificar Java
if ! check_java; then
    echo "📥 Java no encontrado. Instalando..."
    case $MACHINE in
        Mac)
            install_java_mac
            ;;
        Linux)
            install_java_linux
            ;;
        *)
            echo "❌ Sistema operativo no soportado: $MACHINE"
            echo "💡 Por favor instala Java 11 manualmente desde: https://adoptium.net/"
            exit 1
            ;;
    esac
    
    # Verificar Java nuevamente
    if ! check_java; then
        echo "❌ Error instalando Java"
        exit 1
    fi
fi

# Verificar Node.js
if ! check_node; then
    echo "📥 Node.js no encontrado. Instalando..."
    if [ "$MACHINE" = "Linux" ]; then
        install_node
    else
        echo "💡 Por favor instala Node.js manualmente desde: https://nodejs.org/"
        exit 1
    fi
    
    # Verificar Node.js nuevamente
    if ! check_node; then
        echo "❌ Error instalando Node.js"
        exit 1
    fi
fi

# Verificar Gradle
if ! command -v ./gradlew &> /dev/null; then
    echo "📥 Gradle Wrapper no encontrado. Descargando..."
    curl -fsSL https://services.gradle.org/distributions/gradle-8.0-bin.zip -o gradle.zip
    unzip -q gradle.zip
    rm gradle.zip
fi

# Dar permisos de ejecución
chmod +x gradlew

# Compilar el proyecto
echo "🔨 Compilando proyecto..."
./gradlew clean build --quiet

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa!"
else
    echo "❌ Error en la compilación"
    exit 1
fi

# Crear script de inicio
echo "📝 Creando script de inicio..."
cat > start-mcp.sh << 'EOF'
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
EOF

chmod +x start-mcp.sh

echo ""
echo "🎉 ¡Instalación completada!"
echo "=========================="
echo ""
echo "📋 Para usar el MCP Security Analyzer:"
echo "   1. Ejecuta: ./start-mcp.sh"
echo "   2. Abre tu navegador en: http://localhost:6274/"
echo "   3. Usa las herramientas disponibles:"
echo "      - scan_repo: Analiza repositorios (usa 'auto' para búsqueda automática)"
echo "      - analyze_policies: Analiza políticas de desarrollo"
echo ""
echo "🔧 Comandos útiles:"
echo "   - ./start-mcp.sh          # Iniciar el inspector"
echo "   - ./gradlew build         # Recompilar el proyecto"
echo "   - ./gradlew clean         # Limpiar build"
echo ""
echo "📚 Documentación: README.md"
echo "" 