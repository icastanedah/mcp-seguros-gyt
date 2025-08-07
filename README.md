# MCP Security Analyzer

🔍 **Analizador de seguridad y políticas de desarrollo para repositorios**

## 🚀 Instalación Rápida

### Opción 1: Instalación Automática (Recomendada)

#### macOS/Linux
```bash
# Clonar el repositorio
git clone <repository-url>
cd mcp-seguros-gyt

# Ejecutar instalador universal
chmod +x install.sh
./install.sh
```

#### Windows
```cmd
# Clonar el repositorio
git clone <repository-url>
cd mcp-seguros-gyt

# Ejecutar instalador universal
install.bat
```

### Opción 2: Instalación Manual

#### Prerrequisitos

1. **Java 11 o superior**
   ```bash
   # macOS (con Homebrew)
   brew install openjdk@11
   export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"
   
   # Linux (Ubuntu/Debian)
   sudo apt-get install openjdk-11-jdk
   
   # Linux (CentOS/RHEL)
   sudo yum install java-11-openjdk-devel
   
   # Windows
   # Descargar desde: https://adoptium.net/
   # O usar Chocolatey: choco install openjdk11
   ```

2. **Node.js 16 o superior**
   ```bash
   # Descargar desde: https://nodejs.org/
   # O usar nvm:
   curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
   nvm install 16
   nvm use 16
   
   # Windows (Chocolatey)
   choco install nodejs
   ```

#### Compilación

```bash
# macOS/Linux
chmod +x gradlew
./gradlew clean build

# Windows
gradlew.bat clean build
```

## 🎯 Uso

### Iniciar el Inspector MCP

#### macOS/Linux
```bash
# Usar el script de inicio (recomendado)
./start-mcp.sh

# O manualmente
npx @modelcontextprotocol/inspector@0.15.0 java -jar build/libs/mcp-graddle.jar
```

#### Windows
```cmd
# Usar el script de inicio (recomendado)
start-mcp.bat

# O manualmente
npx @modelcontextprotocol/inspector@0.15.0 java -jar build\libs\mcp-graddle.jar
```

### Acceder a la Interfaz Web

1. Abre tu navegador
2. Ve a: `http://localhost:6274/`
3. Usa las herramientas disponibles

## 🛠️ Herramientas Disponibles

### 1. `scan_repo` - Análisis de Seguridad

**Descripción**: Escanea repositorios por vulnerabilidades de seguridad y problemas de desarrollo

**Parámetros**:
- `repo_path`: Path del repositorio a escanear
  - `auto`: Búsqueda automática en ubicaciones comunes
  - `Documents/deyanecast`: Path específico
  - `/ruta/completa`: Path absoluto

**Ejemplo**:
```json
{
  "name": "scan_repo",
  "arguments": {
    "repo_path": "auto"
  }
}
```

### 2. `analyze_policies` - Análisis de Políticas

**Descripción**: Analiza código para verificar cumplimiento de políticas de desarrollo

**Parámetros**:
- `code_path`: Path del archivo o repositorio a analizar

**Ejemplo**:
```json
{
  "name": "analyze_policies",
  "arguments": {
    "code_path": "Documents/deyanecast"
  }
}
```

## 🔍 Funcionalidades

### Búsqueda Automática de Repositorios

El sistema busca automáticamente en:
- `~/Documents`
- `~/Desktop`
- Directorio actual

### Detección de Vulnerabilidades

- **SQL Injection**: Uso de concatenación en queries SQL
- **Hardcoded Passwords**: Contraseñas hardcodeadas
- **Hardcoded Secrets**: Secretos hardcodeados
- **HTTP URLs**: URLs no seguras (HTTP)
- **Command Injection**: Ejecución de comandos
- **Path Traversal**: Traversal de directorios
- **XSS**: Uso de innerHTML
- **Eval**: Uso de eval()
- **Console Log**: console.log() en producción
- **Insecure Fetch**: Fetch a URLs HTTP

### Análisis de Políticas

- **Naming Conventions**: Convenciones de nombres
- **Code Complexity**: Complejidad del código
- **Documentation**: Documentación
- **Error Handling**: Manejo de errores
- **Code Structure**: Estructura del código

## 📊 Límites de Seguridad

- **Tamaño máximo de archivo**: 10MB
- **Longitud máxima de línea**: 1000 caracteres
- **Profundidad máxima de búsqueda**: 3 niveles
- **Número máximo de archivos**: 50 por repositorio
- **Número máximo de líneas**: 1000 por archivo
- **Número máximo de issues**: 20 por archivo
- **Número máximo de repositorios**: 5 en búsqueda automática

## 🔧 Configuración

### Variables de Entorno

```bash
# Configurar Java
export JAVA_HOME="/opt/homebrew/opt/openjdk@11"
export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"

# Configurar Node.js
export PATH="/usr/local/bin:$PATH"
```

### Archivo de Configuración

El sistema usa `mcp-config.properties` para configuración avanzada:

```properties
# Límites de archivos
max.file.size=10485760
max.line.length=1000
max.depth=3

# Codificación
file.encoding=UTF-8

# Separadores
file.separator=/
```

## 🐛 Solución de Problemas

### Error: "Request timed out"

**Causa**: El análisis está tardando demasiado
**Solución**: 
- Usa paths específicos en lugar de `auto`
- Reduce el tamaño de los repositorios
- Verifica que los archivos no sean demasiado grandes

### Error: "Java not found"

**Causa**: Java no está instalado o no está en el PATH
**Solución**:
```bash
# Verificar Java
java -version

# Si no está instalado, seguir las instrucciones de instalación
```

### Error: "Node.js not found"

**Causa**: Node.js no está instalado
**Solución**:
```bash
# Verificar Node.js
node --version

# Si no está instalado, descargar desde: https://nodejs.org/
```

### Error: "Permission denied"

**Causa**: Falta de permisos de ejecución
**Solución**:
```bash
chmod +x gradlew
chmod +x start-mcp.sh
chmod +x install.sh
```

## 📈 Métricas de Rendimiento

- **Tiempo de análisis**: < 30 segundos por repositorio
- **Memoria utilizada**: < 512MB
- **Archivos procesados**: Hasta 50 por repositorio
- **Líneas analizadas**: Hasta 1000 por archivo

## 🤝 Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 🆘 Soporte

- **Issues**: Crear un issue en GitHub
- **Documentación**: Ver `README.md` y `USAGE.md`
- **Configuración**: Ver `mcp-config.properties`

## 🎯 Roadmap

- [ ] Soporte para más lenguajes de programación
- [ ] Análisis de dependencias
- [ ] Integración con CI/CD
- [ ] Reportes en PDF
- [ ] API REST
- [ ] Plugin para IDEs 