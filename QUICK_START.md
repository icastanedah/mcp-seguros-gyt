# MCP Security Analyzer - Guía de Inicio Rápido

## 🚀 Instalación en 5 Minutos

### Paso 1: Clonar el Repositorio

```bash
git clone <repository-url>
cd mcp-seguros-gyt
```

### Paso 2: Instalación Automática

```bash
chmod +x install.sh
./install.sh
```

### Paso 3: Iniciar el Servidor

```bash
./start-mcp.sh
```

### Paso 4: Acceder a la Interfaz

1. Abre tu navegador
2. Ve a: `http://localhost:6274/`
3. ¡Listo para usar!

---

## 🎯 Primeros Pasos

### 1. Análisis de Seguridad Básico

```json
{
  "name": "scan_repo",
  "arguments": {
    "repo_path": "auto"
  }
}
```

**Resultado esperado**:
```
🔍 Buscando repositorios automáticamente...

📁 Analizando: mi-proyecto
✅ Repositorio analizado exitosamente
📊 Vulnerabilidades encontradas: 3
   - 🔴 SQL Injection (Línea 45)
   - 🟡 Hardcoded Password (Línea 23)
   - 🟡 XSS Vulnerability (Línea 67)
```

### 2. Análisis de Políticas

```json
{
  "name": "analyze_policies",
  "arguments": {
    "code_path": "src/main/java"
  }
}
```

**Resultado esperado**:
```
📋 Analizando políticas de desarrollo...

✅ Convenciones de nombres: 95% cumplimiento
✅ Complejidad del código: 87% cumplimiento
⚠️  Documentación: 60% cumplimiento
✅ Manejo de errores: 92% cumplimiento
```

---

## 🔧 Configuración Rápida

### Variables de Entorno

```bash
# macOS
export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

# Linux
export JAVA_HOME="/usr/lib/jvm/java-11-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

### Verificación de Instalación

```bash
# Verificar Java
java -version

# Verificar Node.js
node --version

# Verificar Gradle
./gradlew --version
```

---

## 📊 Ejemplos Prácticos

### Ejemplo 1: Análisis de Repositorio Específico

```bash
# Analizar un repositorio específico
curl -X POST http://localhost:6274/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "scan_repo",
    "arguments": {
      "repo_path": "/Users/usuario/proyectos/mi-app"
    }
  }'
```

### Ejemplo 2: Análisis de Archivo Único

```bash
# Analizar un archivo específico
curl -X POST http://localhost:6274/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "analyze_policies",
    "arguments": {
      "code_path": "src/main/java/org/example/App.java"
    }
  }'
```

### Ejemplo 3: Integración con Script

```bash
#!/bin/bash
# script-analysis.sh

echo "🔍 Iniciando análisis de seguridad..."

# Analizar repositorio actual
RESULT=$(curl -s -X POST http://localhost:6274/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "scan_repo",
    "arguments": {
      "repo_path": "auto"
    }
  }')

echo "$RESULT" > security-report.txt
echo "📄 Reporte guardado en security-report.txt"
```

---

## 🐛 Solución de Problemas

### Error: "Java not found"

```bash
# Solución para macOS
brew install openjdk@11
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

# Solución para Linux
sudo apt-get install openjdk-11-jdk
```

### Error: "Node.js not found"

```bash
# Descargar desde: https://nodejs.org/
# O usar nvm:
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 16
nvm use 16
```

### Error: "Permission denied"

```bash
chmod +x gradlew
chmod +x start-mcp.sh
chmod +x install.sh
```

### Error: "Port already in use"

```bash
# Verificar puerto
lsof -i :6274

# Matar proceso si es necesario
kill -9 <PID>
```

---

## 📈 Monitoreo y Métricas

### Verificar Estado del Servidor

```bash
# Health check
curl http://localhost:6274/health

# Métricas de rendimiento
curl http://localhost:6274/metrics
```

### Logs del Sistema

```bash
# Ver logs en tiempo real
tail -f logs/mcp-server.log

# Ver errores
grep "ERROR" logs/mcp-server.log
```

---

## 🔄 Integración con CI/CD

### GitHub Actions

```yaml
name: Security Analysis

on: [push, pull_request]

jobs:
  security-analysis:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up Java
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Set up Node.js
      uses: actions/setup-node@v2
      with:
        node-version: '16'
    
    - name: Run Security Analysis
      run: |
        chmod +x gradlew
        ./gradlew build
        ./start-mcp.sh &
        sleep 10
        curl -X POST http://localhost:6274/tools/call \
          -H "Content-Type: application/json" \
          -d '{"name": "scan_repo", "arguments": {"repo_path": "auto"}}'
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    stages {
        stage('Security Analysis') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew build'
                sh './start-mcp.sh &'
                sh 'sleep 10'
                sh '''
                    curl -X POST http://localhost:6274/tools/call \
                      -H "Content-Type: application/json" \
                      -d '{"name": "scan_repo", "arguments": {"repo_path": "auto"}}'
                '''
            }
        }
    }
}
```

---

## 🎯 Casos de Uso Comunes

### 1. Análisis Diario

```bash
# Crear script para análisis diario
cat > daily-analysis.sh << 'EOF'
#!/bin/bash
echo "🔍 Análisis diario de seguridad - $(date)"

# Iniciar servidor
./start-mcp.sh &
SERVER_PID=$!

# Esperar a que esté listo
sleep 10

# Ejecutar análisis
RESULT=$(curl -s -X POST http://localhost:6274/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name": "scan_repo", "arguments": {"repo_path": "auto"}}')

# Guardar resultado
echo "$RESULT" > "reports/security-$(date +%Y%m%d).txt"

# Detener servidor
kill $SERVER_PID

echo "✅ Análisis completado"
EOF

chmod +x daily-analysis.sh
```

### 2. Análisis Pre-commit

```bash
# Crear hook de git
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "🔍 Ejecutando análisis de seguridad..."

# Ejecutar análisis rápido
./gradlew build --quiet
./start-mcp.sh &
SERVER_PID=$!
sleep 5

RESULT=$(curl -s -X POST http://localhost:6274/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name": "scan_repo", "arguments": {"repo_path": "."}}')

kill $SERVER_PID

# Verificar si hay vulnerabilidades críticas
if echo "$RESULT" | grep -q "🔴"; then
    echo "❌ Vulnerabilidades críticas encontradas. Commit bloqueado."
    exit 1
fi

echo "✅ Análisis de seguridad pasado"
EOF

chmod +x .git/hooks/pre-commit
```

---

## 📞 Soporte Rápido

### Comandos Útiles

```bash
# Verificar estado
./gradlew status

# Limpiar build
./gradlew clean

# Ejecutar tests
./gradlew test

# Ver ayuda
./gradlew help
```

### Recursos Adicionales

- **Documentación**: `DOCUMENTACION.md`
- **Presentación**: `PRESENTACION.md`
- **Issues**: GitHub Issues
- **Comunidad**: Discord/Slack

---

## 🎉 ¡Listo!

¡Felicitaciones! Ya tienes MCP Security Analyzer funcionando en tu entorno.

### Próximos Pasos:

1. **Explorar** la interfaz web en `http://localhost:6274/`
2. **Probar** con tus propios repositorios
3. **Integrar** en tu flujo de trabajo
4. **Contribuir** al proyecto

### ¿Necesitas Ayuda?

- 📖 Lee la documentación completa
- 🐛 Reporta issues en GitHub
- 💬 Únete a la comunidad

---

**Versión**: 1.0.0  
**Última Actualización**: Diciembre 2024 