package org.example.mcp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class SecurityAnalyzer {
    private final Map<String, List<SecurityRule>> rules;
    
    public SecurityAnalyzer() {
        this.rules = initializeRules();
    }
    
    private Map<String, List<SecurityRule>> initializeRules() {
        Map<String, List<SecurityRule>> ruleMap = new HashMap<>();
        
        List<SecurityRule> javaRules = new ArrayList<>();
        javaRules.add(new SecurityRule("SQL_INJECTION", "\".*SELECT.*\\+.*\"", "CRÍTICO", "Usar PreparedStatement"));
        javaRules.add(new SecurityRule("HARDCODED_PASSWORD", "password\\s*=\\s*\"[^\"]+\"", "ALTO", "Usar variables de entorno"));
        javaRules.add(new SecurityRule("HARDCODED_SECRET", "(secret|token|key)\\s*=\\s*\"[^\"]+\"", "ALTO", "Usar configuración externa"));
        javaRules.add(new SecurityRule("HTTP_URL", "\"http://[^\"]*\"", "MEDIO", "Usar HTTPS"));
        ruleMap.put("java", javaRules);
        
        List<SecurityRule> jsRules = new ArrayList<>();
        jsRules.add(new SecurityRule("XSS_INNERHTML", "innerHTML\\s*=", "CRÍTICO", "Usar textContent"));
        jsRules.add(new SecurityRule("EVAL_JS", "eval\\(", "ALTO", "Evitar eval()"));
        jsRules.add(new SecurityRule("CONSOLE_LOG", "console\\.log\\(", "BAJO", "Remover en producción"));
        ruleMap.put("js", jsRules);
        
        return ruleMap;
    }
    
    public String scanRepository(String repoPath) {
        try {
            Path path = Paths.get(repoPath);
            if (!Files.exists(path)) {
                return "❌ Repositorio no encontrado: " + repoPath;
            }
            
            List<SecurityIssue> issues = new ArrayList<>();
            scanDirectory(path, issues);
            
            return formatResults(issues, repoPath);
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    private void scanDirectory(Path dir, List<SecurityIssue> issues) throws IOException {
        Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(this::isSupportedFile)
            .forEach(file -> scanFile(file, issues));
    }
    
    private boolean isSupportedFile(Path file) {
        String name = file.toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts");
    }
    
    private void scanFile(Path file, List<SecurityIssue> issues) {
        try {
            String content = new String(Files.readAllBytes(file));
            String extension = getFileExtension(file);
            List<SecurityRule> fileRules = rules.get(extension);
            if (fileRules == null) return;
            
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                for (SecurityRule rule : fileRules) {
                    if (Pattern.compile(rule.pattern).matcher(lines[i]).find()) {
                        issues.add(new SecurityIssue(
                            rule.name, rule.severity, rule.solution,
                            file.toString(), i + 1, lines[i].trim()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            // Ignorar archivos no legibles
        }
    }
    
    private String getFileExtension(Path file) {
        String name = file.toString();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }
    
    private String formatResults(List<SecurityIssue> issues, String repoPath) {
        if (issues.isEmpty()) {
            return "✅ No se encontraron vulnerabilidades en: " + repoPath;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("🔍 ANÁLISIS DE SEGURIDAD\n");
        result.append("📁 ").append(repoPath).append("\n");
        result.append("📊 Issues encontrados: ").append(issues.size()).append("\n\n");
        
        for (SecurityIssue issue : issues) {
            result.append(formatIssue(issue)).append("\n");
        }
        
        return result.toString();
    }
    
    private String formatIssue(SecurityIssue issue) {
        return String.format("%s %s\n📁 %s:%d\n💻 %s\n🔧 %s\n",
            getSeverityIcon(issue.severity), issue.name,
            issue.file.substring(issue.file.lastIndexOf('\\') + 1), issue.line,
            issue.code,
            issue.solution);
    }
    
    private String getSeverityIcon(String severity) {
        switch (severity) {
            case "CRÍTICO": return "🔴";
            case "ALTO": return "🟠";
            case "MEDIO": return "🟡";
            default: return "🔵";
        }
    }
    
    static class SecurityRule {
        final String name, pattern, severity, solution;
        
        SecurityRule(String name, String pattern, String severity, String solution) {
            this.name = name;
            this.pattern = pattern;
            this.severity = severity;
            this.solution = solution;
        }
    }
    
    static class SecurityIssue {
        final String name, severity, solution, file, code;
        final int line;
        
        SecurityIssue(String name, String severity, String solution, 
                     String file, int line, String code) {
            this.name = name;
            this.severity = severity;
            this.solution = solution;
            this.file = file;
            this.line = line;
            this.code = code;
        }
    }
}