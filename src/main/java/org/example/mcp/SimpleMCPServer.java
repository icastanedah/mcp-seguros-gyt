package org.example.mcp;

import org.example.mcp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class SimpleMCPServer {
    private final ObjectMapper objectMapper;
    private final SecurityAnalyzer securityAnalyzer;
    private final ChecklistAuditor checklistAuditor;
    private final GeminiSecurityAnalyzer geminiAnalyzer;
    
    public SimpleMCPServer() {
        this.objectMapper = new ObjectMapper();
        this.securityAnalyzer = new SecurityAnalyzer();
        this.checklistAuditor = new ChecklistAuditor();
        this.geminiAnalyzer = new GeminiSecurityAnalyzer();
    }
    
    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                try {
                    MCPMessage request = objectMapper.readValue(line, MCPMessage.class);
                    String response = handleRequest(request);
                    
                    writer.write(response);
                    writer.newLine();
                    writer.flush();
                } catch (Exception e) {
                    String errorResponse = "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
                    writer.write(errorResponse);
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException e) {
            // Silent error
        }
    }
    
    private String handleRequest(MCPMessage request) throws Exception {
        String method = request.getMethod();
        Object id = request.getId();
        
        switch (method) {
            case "initialize":
                return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                       ",\"result\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{\"tools\":{}},\"serverInfo\":{\"name\":\"Security Analyzer\",\"version\":\"1.0.0\"}}}";
                       
            case "tools/list":
                return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                       ",\"result\":{\"tools\":[" +
                       "{\"name\":\"scan_repo\",\"description\":\"Escanea repositorio por vulnerabilidades\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"repo_path\":{\"type\":\"string\",\"description\":\"Path del repositorio\"}},\"required\":[\"repo_path\"]}}," +
                       "{\"name\":\"checklist_audit\",\"description\":\"Audita proyecto contra checklist de desarrollo\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"project_path\":{\"type\":\"string\",\"description\":\"Path del proyecto\"}},\"required\":[\"project_path\"]}}," +
                       "{\"name\":\"check_policies\",\"description\":\"Analiza políticas de desarrollo con IA\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"repo_path\":{\"type\":\"string\",\"description\":\"Path del repositorio\"}},\"required\":[\"repo_path\"]}}" +
                       "]}}"; 
                       
            case "tools/call":
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) request.getParams();
                String toolName = (String) params.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> args = (Map<String, Object>) params.get("arguments");
                
                if ("scan_repo".equals(toolName)) {
                    String repoPath = (String) args.get("repo_path");
                    String scanResult = securityAnalyzer.scanRepository(repoPath);
                    String escapedResult = objectMapper.writeValueAsString(scanResult);
                    return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                           ",\"result\":{\"content\":[{\"type\":\"text\",\"text\":" + escapedResult + "}]}}";
                } else if ("checklist_audit".equals(toolName)) {
                    String projectPath = (String) args.get("project_path");
                    String auditResult = checklistAuditor.auditProject(projectPath);
                    String escapedResult = objectMapper.writeValueAsString(auditResult);
                    return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                           ",\"result\":{\"content\":[{\"type\":\"text\",\"text\":" + escapedResult + "}]}}";
                } else if ("check_policies".equals(toolName)) {
                    String repoPath = (String) args.get("repo_path");
                    String analysisResult = geminiAnalyzer.analyzeWithAI(repoPath);
                    String escapedResult = objectMapper.writeValueAsString(analysisResult);
                    return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                           ",\"result\":{\"content\":[{\"type\":\"text\",\"text\":" + escapedResult + "}]}}";
                }
                
                return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                       ",\"error\":{\"code\":-32601,\"message\":\"Tool not found\"}}";
                       
            default:
                return "{\"jsonrpc\":\"2.0\",\"id\":" + objectMapper.writeValueAsString(id) + 
                       ",\"error\":{\"code\":-32601,\"message\":\"Method not found\"}}";
        }
    }
    
    public static void main(String[] args) {
        new SimpleMCPServer().start();
    }
}