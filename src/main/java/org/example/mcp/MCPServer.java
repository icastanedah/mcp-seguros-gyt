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
        Map<String, Object> calcProps = new HashMap<>();
        Map<String, Object> exprProp = new HashMap<>();
        exprProp.put("type", "string");
        exprProp.put("description", "Mathematical expression to evaluate");
        calcProps.put("expression", exprProp);
        
        Map<String, Object> calcSchema = new HashMap<>();
        calcSchema.put("type", "object");
        calcSchema.put("properties", calcProps);
        List<String> calcRequired = new ArrayList<>();
        calcRequired.add("expression");
        calcSchema.put("required", calcRequired);

        tools.put("calculator", new Tool(
                "calculator",
                "Evaluate mathematical expressions",
                calcSchema
        ));

        // Ejemplo de herramienta: echo
        Map<String, Object> echoProps = new HashMap<>();
        Map<String, Object> msgProp = new HashMap<>();
        msgProp.put("type", "string");
        msgProp.put("description", "Message to echo back");
        echoProps.put("message", msgProp);
        
        Map<String, Object> echoSchema = new HashMap<>();
        echoSchema.put("type", "object");
        echoSchema.put("properties", echoProps);
        List<String> echoRequired = new ArrayList<>();
        echoRequired.add("message");
        echoSchema.put("required", echoRequired);

        tools.put("echo", new Tool(
                "echo",
                "Echo back the provided message",
                echoSchema
        ));

        // Herramienta de seguridad
        Map<String, Object> secProps = new HashMap<>();
        Map<String, Object> pathProp = new HashMap<>();
        pathProp.put("type", "string");
        pathProp.put("description", "Path del repositorio a escanear");
        secProps.put("repo_path", pathProp);
        
        Map<String, Object> securitySchema = new HashMap<>();
        securitySchema.put("type", "object");
        securitySchema.put("properties", secProps);
        List<String> secRequired = new ArrayList<>();
        secRequired.add("repo_path");
        securitySchema.put("required", secRequired);

        tools.put("scan_repo", new Tool(
                "scan_repo",
                "Escanea repositorio por vulnerabilidades de seguridad",
                securitySchema
        ));

        // Herramienta de análisis de políticas de código
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

        tools.put("check_policies", new Tool(
                "check_policies",
                "Analiza código para verificar cumplimiento de políticas de desarrollo",
                policySchema
        ));

        // Herramienta de análisis de archivo específico
        tools.put("analyze_file", new Tool(
                "analyze_file",
                "Analiza un archivo específico contra políticas de desarrollo",
                policySchema
        ));
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    MCPMessage request = objectMapper.readValue(line, MCPMessage.class);
                    
                    // Asegurar que el request tenga un ID válido
                    if (request.getId() == null) {
                        request.setId("default-id");
                    }
                    
                    MCPResponse response = handleRequest(request);

                    if (response != null) {
                        String responseJson = objectMapper.writeValueAsString(response);
                        writer.write(responseJson);
                        writer.newLine();
                        writer.flush();
                    }
                } catch (Exception e) {
                    // Crear respuesta de error válida con ID por defecto
                    MCPResponse errorResponse = new MCPResponse("parse-error");
                    errorResponse.setError(new MCPError(-32700, "Parse error: " + e.getMessage()));

                    try {
                        String errorJson = objectMapper.writeValueAsString(errorResponse);
                        writer.write(errorJson);
                        writer.newLine();
                        writer.flush();
                    } catch (Exception writeError) {
                        // Error crítico - no podemos escribir la respuesta
                        System.err.println("Critical error writing response: " + writeError.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            // Error silencioso
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
            
            switch (method) {
                case "initialize":
                    response.setResult(handleInitialize());
                    break;
                case "tools/list":
                    response.setResult(handleToolsList());
                    break;
                case "tools/call":
                    response.setResult(handleToolCall(request.getParams()));
                    break;
                default:
                    response.setError(new MCPError(-32601, "Method not found: " + method));
            }
        } catch (Exception e) {
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
        Map<String, Object> result = new HashMap<>();
        result.put("tools", new ArrayList<>(tools.values()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolCall(Object params) {
        Map<String, Object> paramMap = (Map<String, Object>) params;
        String toolName = (String) paramMap.get("name");
        Map<String, Object> arguments = (Map<String, Object>) paramMap.get("arguments");

        if (!tools.containsKey(toolName)) {
            throw new RuntimeException("Tool not found: " + toolName);
        }

        // Ejecutar la herramienta
        String result = executeTool(toolName, arguments);

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", result);
        content.add(textContent);
        response.put("content", content);
        return response;
    }

    private String executeTool(String toolName, Map<String, Object> arguments) {
        switch (toolName) {
            case "calculator":
                return evaluateExpression((String) arguments.get("expression"));
            case "echo":
                return "Echo: " + arguments.get("message");
            case "scan_repo":
                return securityAnalyzer.scanRepository((String) arguments.get("repo_path"));
            case "check_policies":
                return policyAnalyzer.analyzeRepository((String) arguments.get("code_path"));
            case "analyze_file":
                return policyAnalyzer.analyzeCode((String) arguments.get("code_path"));
            default:
                return "Unknown tool: " + toolName;
        }
    }

    private String evaluateExpression(String expression) {
        try {
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
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        new MCPServer().start();
    }
}