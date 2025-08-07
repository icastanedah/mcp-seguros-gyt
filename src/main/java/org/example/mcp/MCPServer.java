package org.example.mcp;

import org.example.mcp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class MCPServer {
    private final ObjectMapper objectMapper;
    private final Map<String, Tool> tools;
    private final SecurityAnalyzer securityAnalyzer;
    private final PolicyAnalyzer policyAnalyzer;

    public MCPServer() {
        this.objectMapper = new ObjectMapper();
        this.tools = new HashMap<>();
        this.securityAnalyzer = new SecurityAnalyzer();
        this.policyAnalyzer = new PolicyAnalyzer();
        initializeTools();
    }

    private void initializeTools() {
        // Herramienta de seguridad - SIMPLIFICADA
        Map<String, Object> secProps = new HashMap<>();
        Map<String, Object> pathProp = new HashMap<>();
        pathProp.put("type", "string");
        pathProp.put("description", "Path del repositorio a escanear (o 'auto' para búsqueda automática)");
        secProps.put("repo_path", pathProp);
        
        Map<String, Object> securitySchema = new HashMap<>();
        securitySchema.put("type", "object");
        securitySchema.put("properties", secProps);
        List<String> secRequired = new ArrayList<>();
        secRequired.add("repo_path");
        securitySchema.put("required", secRequired);

        tools.put("scan_repo", new Tool(
                "scan_repo",
                "Escanea repositorio por vulnerabilidades de seguridad y problemas de desarrollo",
                securitySchema
        ));

        // Herramienta de análisis de políticas
        Map<String, Object> policyProps = new HashMap<>();
        Map<String, Object> codePathProp = new HashMap<>();
        codePathProp.put("type", "string");
        codePathProp.put("description", "Path del archivo o repositorio a analizar");
        policyProps.put("code_path", codePathProp);
        
        Map<String, Object> policySchema = new HashMap<>();
        policySchema.put("type", "object");
        policySchema.put("properties", policyProps);
        List<String> policyRequired = new ArrayList<>();
        policyRequired.add("code_path");
        policySchema.put("required", policyRequired);

        tools.put("analyze_policies", new Tool(
                "analyze_policies",
                "Analiza código para verificar cumplimiento de políticas de desarrollo",
                policySchema
        ));
    }

    public void start() {
        System.err.println("🚀 MCP Server iniciando...");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    System.err.println("📥 Recibido: " + line);
                    
                    MCPMessage request = objectMapper.readValue(line, MCPMessage.class);
                    MCPResponse response = handleRequest(request);
                    
                    String responseJson = objectMapper.writeValueAsString(response);
                    System.err.println("📤 Enviando: " + responseJson);
                    
                    writer.write(responseJson);
                    writer.newLine();
                    writer.flush();
                    
                } catch (Exception e) {
                    System.err.println("❌ Error procesando request: " + e.getMessage());
                    MCPResponse errorResponse = new MCPResponse("error");
                    errorResponse.setError(new MCPError(-32603, "Internal error: " + e.getMessage()));
                    
                    try {
                        String errorJson = objectMapper.writeValueAsString(errorResponse);
                        writer.write(errorJson);
                        writer.newLine();
                        writer.flush();
                    } catch (Exception ex) {
                        System.err.println("❌ Error enviando error response: " + ex.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error de I/O: " + e.getMessage());
        }
    }

    private MCPResponse handleRequest(MCPMessage request) {
        Object requestId = request.getId() != null ? request.getId() : "unknown";
        MCPResponse response = new MCPResponse(requestId);

        try {
            String method = request.getMethod();
            if (method == null) {
                response.setError(new MCPError(-32600, "Invalid Request: method is required"));
                return response;
            }
            
            System.err.println("🔧 Procesando método: " + method);
            
            switch (method) {
                case "initialize":
                    System.err.println("✅ Inicializando...");
                    response.setResult(handleInitialize());
                    break;
                case "tools/list":
                    System.err.println("📋 Listando herramientas...");
                    response.setResult(handleToolsList());
                    break;
                case "tools/call":
                    System.err.println("🛠️ Ejecutando herramienta...");
                    response.setResult(handleToolCall(request.getParams()));
                    break;
                default:
                    System.err.println("❌ Método no soportado: " + method);
                    response.setError(new MCPError(-32601, "Method not found: " + method));
            }
        } catch (Exception e) {
            System.err.println("❌ Error en handleRequest: " + e.getMessage());
            response.setError(new MCPError(-32603, "Internal error: " + e.getMessage()));
        }
        
        return response;
    }

    private Map<String, Object> handleInitialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<>());
        result.put("capabilities", capabilities);
        
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "Java MCP Server");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);
        
        return result;
    }

    private Map<String, Object> handleToolsList() {
        System.err.println("📋 Preparando lista de herramientas...");
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> toolsList = new ArrayList<>();
        
        for (Tool tool : tools.values()) {
            Map<String, Object> toolMap = new HashMap<>();
            toolMap.put("name", tool.getName());
            toolMap.put("description", tool.getDescription());
            toolMap.put("inputSchema", tool.getInputSchema());
            toolsList.add(toolMap);
            System.err.println("➕ Agregada herramienta: " + tool.getName());
        }
        
        result.put("tools", toolsList);
        System.err.println("✅ Retornando " + toolsList.size() + " herramientas");
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolCall(Object params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!(params instanceof Map)) {
                List<Map<String, Object>> content = new ArrayList<>();
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", "❌ Error: Parámetros inválidos");
                content.add(textContent);
                result.put("content", content);
                return result;
            }
            
            Map<String, Object> paramsMap = (Map<String, Object>) params;
            String toolName = (String) paramsMap.get("name");
            Map<String, Object> arguments = (Map<String, Object>) paramsMap.get("arguments");
            
            if (toolName == null) {
                List<Map<String, Object>> content = new ArrayList<>();
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", "❌ Error: Nombre de herramienta requerido");
                content.add(textContent);
                result.put("content", content);
                return result;
            }
            
            if (arguments == null) {
                arguments = new HashMap<>();
            }
            
            System.err.println("🛠️ Ejecutando herramienta: " + toolName + " con argumentos: " + arguments);
            
            String toolResult = executeTool(toolName, arguments);
            
            // Crear el content como un array con un objeto de texto
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", toolResult);
            content.add(textContent);
            result.put("content", content);
            
        } catch (Exception e) {
            System.err.println("❌ Error ejecutando herramienta: " + e.getMessage());
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "❌ Error ejecutando herramienta: " + e.getMessage());
            content.add(textContent);
            result.put("content", content);
        }
        
        return result;
    }

    private String executeTool(String toolName, Map<String, Object> arguments) {
        System.err.println("🔍 Ejecutando: " + toolName);
        
        switch (toolName) {
            case "scan_repo":
                String repoPath = (String) arguments.get("repo_path");
                if (repoPath == null) {
                    return "❌ Error: repo_path es requerido";
                }
                System.err.println("🔍 Escaneando repositorio: " + repoPath);
                return securityAnalyzer.scanRepository(repoPath);
                
            case "analyze_policies":
                String codePath = (String) arguments.get("code_path");
                if (codePath == null) {
                    return "❌ Error: code_path es requerido";
                }
                System.err.println("📋 Analizando políticas: " + codePath);
                return policyAnalyzer.analyzeRepository(codePath);
                
            default:
                return "❌ Herramienta desconocida: " + toolName;
        }
    }

    private String evaluateExpression(String expression) {
        try {
            // Validar expresión para evitar inyección de código
            if (!expression.matches("^[0-9\\s+\\-*/().]+$")) {
                return "Error: Invalid expression format - only numbers and basic operators allowed";
            }
            
            if (expression.matches("\\d+\\s*[+\\-*/]\\s*\\d+")) {
                String[] parts = expression.split("\\s*([+\\-*/])\\s*");
                if (parts.length >= 2) {
                    double a = Double.parseDouble(parts[0]);
                    double b = Double.parseDouble(parts[1]);
                    String cleanExpr = expression.replaceAll("\\d|\\s", "");
                    if (cleanExpr.length() > 0) {
                        char op = cleanExpr.charAt(0);
                        switch (op) {
                            case '+': return String.valueOf(a + b);
                            case '-': return String.valueOf(a - b);
                            case '*': return String.valueOf(a * b);
                            case '/': 
                                if (b != 0) return String.valueOf(a / b);
                                else return "Error: Division by zero";
                            default: return "Error: Unknown operator";
                        }
                    }
                }
            }
            return "Error: Invalid expression format";
        } catch (NumberFormatException e) {
            return "Error: Invalid number format";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        new MCPServer().start();
    }
}