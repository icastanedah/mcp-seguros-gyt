# MCP Security Analyzer - Diagramas Técnicos

## 🏗️ Arquitectura General

### Diagrama de Alto Nivel

```mermaid
graph TB
    A[IA Client<br/>Claude/GPT] --> B[MCP Inspector<br/>Web UI]
    B --> C[MCP Server<br/>Java]
    C --> D[Security Analyzer]
    C --> E[Policy Analyzer]
    C --> F[AI Security Analyzer]
    
    D --> G[Vulnerability Detection]
    E --> H[Policy Compliance]
    F --> I[Intelligent Analysis]
    
    G --> J[Security Report]
    H --> K[Policy Report]
    I --> L[AI Report]
    
    J --> M[Final Report]
    K --> M
    L --> M
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style C fill:#e8f5e8
    style M fill:#fff3e0
```

### Flujo de Datos

```mermaid
sequenceDiagram
    participant IA as IA Client
    participant Inspector as MCP Inspector
    participant Server as MCP Server
    participant Security as Security Analyzer
    participant Policy as Policy Analyzer
    participant AI as AI Analyzer
    
    IA->>Inspector: Request Analysis
    Inspector->>Server: MCP Message
    Server->>Security: Scan Repository
    Server->>Policy: Analyze Policies
    Server->>AI: Intelligent Analysis
    
    Security-->>Server: Security Results
    Policy-->>Server: Policy Results
    AI-->>Server: AI Results
    
    Server-->>Inspector: Combined Report
    Inspector-->>IA: Final Report
```

## 🔍 Componentes Detallados

### MCP Server Architecture

```mermaid
graph LR
    A[MCPServer] --> B[Tool Registry]
    A --> C[Message Handler]
    A --> D[Security Manager]
    
    B --> E[scan_repo Tool]
    B --> F[analyze_policies Tool]
    
    C --> G[Request Parser]
    C --> H[Response Builder]
    
    D --> I[Input Validation]
    D --> J[Path Sanitization]
    
    style A fill:#ffeb3b
    style B fill:#4caf50
    style C fill:#2196f3
    style D fill:#f44336
```

### Security Analyzer Flow

```mermaid
flowchart TD
    A[Start Scan] --> B{Path Valid?}
    B -->|No| C[Return Error]
    B -->|Yes| D[Scan Directory]
    
    D --> E[Filter Files]
    E --> F[Read File Content]
    F --> G[Apply Security Rules]
    
    G --> H{Match Found?}
    H -->|No| I[Next File]
    H -->|Yes| J[Record Issue]
    
    I --> K{More Files?}
    K -->|Yes| F
    K -->|No| L[Generate Report]
    
    J --> I
    
    style A fill:#4caf50
    style C fill:#f44336
    style L fill:#2196f3
```

### Policy Analyzer Flow

```mermaid
flowchart TD
    A[Start Analysis] --> B[Load Policy Rules]
    B --> C[Scan Code Files]
    C --> D[Parse Code Structure]
    
    D --> E[Check Naming Conventions]
    D --> F[Check Code Complexity]
    D --> G[Check Documentation]
    D --> H[Check Error Handling]
    
    E --> I[Record Violations]
    F --> I
    G --> I
    H --> I
    
    I --> J[Calculate Compliance Score]
    J --> K[Generate Policy Report]
    
    style A fill:#4caf50
    style K fill:#2196f3
```

## 🤖 Integración con IA

### AI Analysis Engine

```mermaid
graph TB
    A[AI Security Analyzer] --> B[Base Analyzer]
    A --> C[AI Engine]
    A --> D[Context Extractor]
    
    B --> E[Rule-based Analysis]
    C --> F[Intelligent Analysis]
    D --> G[Project Context]
    
    E --> H[Security Issues]
    F --> I[AI Recommendations]
    G --> J[Technology Stack]
    G --> K[Risk Assessment]
    
    H --> L[Combined Report]
    I --> L
    J --> L
    K --> L
    
    style A fill:#9c27b0
    style L fill:#ff9800
```

### Context Extraction Process

```mermaid
flowchart LR
    A[Repository Path] --> B[Detect Project Type]
    B --> C[Count Files]
    B --> D[Detect Technologies]
    B --> E[Calculate Risk Level]
    
    C --> F[Project Context]
    D --> F
    E --> F
    
    F --> G[AI Analysis Engine]
    G --> H[Intelligent Report]
    
    style A fill:#4caf50
    style F fill:#2196f3
    style H fill:#ff9800
```

## 📄 Procesamiento de Documentos

### File Processing Pipeline

```mermaid
graph TD
    A[Input Path] --> B[Path Validation]
    B --> C[Directory Scan]
    C --> D[File Filtering]
    
    D --> E{File Type?}
    E -->|Java| F[Java Analyzer]
    E -->|JavaScript| G[JS Analyzer]
    E -->|Python| H[Python Analyzer]
    E -->|SQL| I[SQL Analyzer]
    E -->|Config| J[Config Analyzer]
    
    F --> K[Security Rules]
    G --> K
    H --> K
    I --> K
    J --> K
    
    K --> L[Issue Detection]
    L --> M[Report Generation]
    
    style A fill:#4caf50
    style M fill:#2196f3
```

### Security Rule Engine

```mermaid
graph LR
    A[File Content] --> B[Rule Engine]
    B --> C[SQL Injection Rules]
    B --> D[Password Rules]
    B --> E[XSS Rules]
    B --> F[Command Injection Rules]
    B --> G[Path Traversal Rules]
    
    C --> H[Issue Detector]
    D --> H
    E --> H
    F --> H
    G --> H
    
    H --> I[Issue Reporter]
    I --> J[Final Report]
    
    style A fill:#4caf50
    style J fill:#2196f3
```

## 🔄 Flujo de Trabajo MCP

### MCP Protocol Flow

```mermaid
sequenceDiagram
    participant Client as MCP Client
    participant Server as MCP Server
    participant Tools as Tools Registry
    
    Client->>Server: initialize
    Server-->>Client: capabilities
    
    Client->>Server: tools/list
    Server-->>Client: available tools
    
    Client->>Server: tools/call
    Server->>Tools: execute tool
    Tools-->>Server: result
    Server-->>Client: response
    
    Note over Client,Server: Error handling
    Server-->>Client: error (if any)
```

### Tool Execution Flow

```mermaid
flowchart TD
    A[Tool Call Request] --> B[Validate Parameters]
    B --> C{Valid?}
    C -->|No| D[Return Error]
    C -->|Yes| E[Execute Tool]
    
    E --> F[Security Analyzer]
    E --> G[Policy Analyzer]
    
    F --> H[Process Results]
    G --> H
    
    H --> I[Format Response]
    I --> J[Return to Client]
    
    style A fill:#4caf50
    style D fill:#f44336
    style J fill:#2196f3
```

## 🎯 Casos de Uso

### Análisis de Seguridad

```mermaid
graph TD
    A[Repository Input] --> B[Auto Discovery]
    B --> C[File Scanning]
    C --> D[Pattern Matching]
    
    D --> E{Vulnerabilities?}
    E -->|Yes| F[Record Issues]
    E -->|No| G[Clean Report]
    
    F --> H[Severity Assessment]
    H --> I[Recommendations]
    I --> J[Final Security Report]
    
    G --> J
    
    style A fill:#4caf50
    style F fill:#f44336
    style J fill:#2196f3
```

### Análisis de Políticas

```mermaid
graph TD
    A[Code Input] --> B[Policy Rules Engine]
    B --> C[Code Structure Analysis]
    C --> D[Compliance Check]
    
    D --> E{Compliance Issues?}
    E -->|Yes| F[Record Violations]
    E -->|No| G[Compliant Report]
    
    F --> H[Compliance Score]
    H --> I[Recommendations]
    I --> J[Final Policy Report]
    
    G --> J
    
    style A fill:#4caf50
    style F fill:#ff9800
    style J fill:#2196f3
```

## 🔧 Configuración y Despliegue

### Deployment Architecture

```mermaid
graph TB
    A[User] --> B[Web Browser]
    B --> C[MCP Inspector<br/>localhost:6274]
    C --> D[MCP Server<br/>Java Process]
    
    D --> E[Security Analyzer]
    D --> F[Policy Analyzer]
    D --> G[AI Analyzer]
    
    E --> H[File System]
    F --> H
    G --> H
    
    style A fill:#e1f5fe
    style C fill:#f3e5f5
    style D fill:#e8f5e8
```

### CI/CD Integration

```mermaid
graph LR
    A[Git Push] --> B[CI/CD Pipeline]
    B --> C[Build Project]
    C --> D[Start MCP Server]
    D --> E[Run Security Analysis]
    E --> F[Generate Report]
    F --> G[Store Results]
    G --> H[Notify Team]
    
    style A fill:#4caf50
    style H fill:#2196f3
```

## 📊 Métricas y Monitoreo

### Performance Metrics

```mermaid
graph TD
    A[Request] --> B[Start Timer]
    B --> C[Process Request]
    C --> D[End Timer]
    D --> E[Calculate Metrics]
    
    E --> F[Response Time]
    E --> G[Memory Usage]
    E --> H[CPU Usage]
    E --> I[Error Rate]
    
    F --> J[Metrics Dashboard]
    G --> J
    H --> J
    I --> J
    
    style A fill:#4caf50
    style J fill:#2196f3
```

### Error Handling Flow

```mermaid
flowchart TD
    A[Request] --> B{Valid Input?}
    B -->|No| C[Input Validation Error]
    B -->|Yes| D{Path Exists?}
    
    D -->|No| E[Path Not Found Error]
    D -->|Yes| F{File Readable?}
    
    F -->|No| G[Permission Error]
    F -->|Yes| H[Process Request]
    
    H --> I{Processing Success?}
    I -->|No| J[Processing Error]
    I -->|Yes| K[Success Response]
    
    style C fill:#f44336
    style E fill:#f44336
    style G fill:#f44336
    style J fill:#f44336
    style K fill:#4caf50
```

---

## 📝 Notas de los Diagramas

### Convenciones Utilizadas:

- **🟢 Verde**: Procesos exitosos/entrada
- **🔵 Azul**: Procesamiento/transformación
- **🟡 Amarillo**: Componentes principales
- **🔴 Rojo**: Errores/vulnerabilidades
- **🟠 Naranja**: Advertencias/violaciones
- **🟣 Morado**: Componentes de IA

### Herramientas de Generación:

- **Mermaid**: Diagramas de flujo y secuencia
- **PlantUML**: Diagramas de arquitectura
- **Draw.io**: Diagramas de alto nivel

### Mantenimiento:

- Actualizar diagramas cuando cambie la arquitectura
- Mantener consistencia en colores y estilos
- Documentar cambios en versiones 