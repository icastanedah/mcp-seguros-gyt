package org.example.mcp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class PolicyAnalyzer {
    private final Map<String, List<PolicyRule>> rules;
    
    public PolicyAnalyzer() {
        this.rules = initializePolicies();
    }
    
    private Map<String, List<PolicyRule>> initializePolicies() {
        Map<String, List<PolicyRule>> policyMap = new HashMap<>();
        
        List<PolicyRule> javaRules = new ArrayList<>();
        // Reglas del spreadsheet de políticas de desarrollo
        javaRules.add(new PolicyRule("CLASE_CAMELCASE", "^class\\s+[a-z]", "CRÍTICO", "Nombres de clases deben usar PascalCase"));
        javaRules.add(new PolicyRule("METODO_CAMELCASE", "^\\s*(public|private|protected)\\s+\\w+\\s+[A-Z]", "ALTO", "Nombres de métodos deben usar camelCase"));
        javaRules.add(new PolicyRule("VARIABLE_CAMELCASE", "^\\s*(int|String|boolean|double|float)\\s+[A-Z]", "ALTO", "Variables deben usar camelCase"));
        javaRules.add(new PolicyRule("CONSTANTE_UPPERCASE", "static\\s+final\\s+\\w+\\s+[a-z]", "MEDIO", "Constantes deben usar UPPER_CASE"));
        javaRules.add(new PolicyRule("JAVADOC_PUBLICO", "^\\s*public\\s+(class|interface|enum)(?!.*\\/\\*\\*)", "ALTO", "Clases públicas requieren JavaDoc"));
        javaRules.add(new PolicyRule("METODO_PUBLICO_JAVADOC", "^\\s*public\\s+\\w+\\s+\\w+\\s*\\((?!.*\\/\\*\\*)", "MEDIO", "Métodos públicos requieren JavaDoc"));
        javaRules.add(new PolicyRule("LINEAS_METODO", "\\{[\\s\\S]{1000,}?\\}", "MEDIO", "Método excede 50 líneas, considerar refactoring"));
        javaRules.add(new PolicyRule("PARAMETROS_METODO", "\\([^)]*,[^)]*,[^)]*,[^)]*,[^)]*,", "MEDIO", "Método tiene más de 5 parámetros"));
        javaRules.add(new PolicyRule("NUMERO_MAGICO", "\\b\\d{2,}\\b(?!\\s*[;,)])", "MEDIO", "Usar constantes nombradas en lugar de números mágicos"));
        javaRules.add(new PolicyRule("IMPORT_WILDCARD", "import\\s+\\w+\\.\\*;", "BAJO", "Evitar imports con wildcard (*)"));
        javaRules.add(new PolicyRule("EXCEPTION_GENERICA", "catch\\s*\\(\\s*Exception", "ALTO", "Capturar excepciones específicas, no Exception genérica"));
        javaRules.add(new PolicyRule("SYSTEM_OUT", "System\\.out\\.print", "BAJO", "Usar logger en lugar de System.out"));
        javaRules.add(new PolicyRule("TODO_FIXME", "//\\s*(TODO|FIXME)", "BAJO", "Resolver comentarios TODO/FIXME antes de producción"));
        javaRules.add(new PolicyRule("HARDCODED_STRING", "\"[^\"]{20,}\"", "MEDIO", "Strings largos deben externalizarse"));
        javaRules.add(new PolicyRule("COMPLEJIDAD_CICLOMATICA", "(if|while|for|switch|catch).*\\{[\\s\\S]*?(if|while|for|switch|catch).*\\{[\\s\\S]*?(if|while|for|switch|catch)", "ALTO", "Complejidad ciclomática alta, refactorizar"));
        policyMap.put("java", javaRules);
        
        List<PolicyRule> jsRules = new ArrayList<>();
        jsRules.add(new PolicyRule("VAR_DEPRECATED", "\\bvar\\s+", "ALTO", "Usar let/const en lugar de var"));
        jsRules.add(new PolicyRule("FUNCTION_CAMELCASE", "function\\s+[A-Z]", "MEDIO", "Funciones deben usar camelCase"));
        jsRules.add(new PolicyRule("CONSOLE_LOG_PROD", "console\\.(log|debug|info)", "BAJO", "Remover console.log en producción"));
        jsRules.add(new PolicyRule("EVAL_FORBIDDEN", "\\beval\\s*\\(", "CRÍTICO", "Prohibido uso de eval()"));
        jsRules.add(new PolicyRule("STRICT_MODE", "^(?!.*'use strict')", "MEDIO", "Usar 'use strict' al inicio del archivo"));
        policyMap.put("js", jsRules);
        
        return policyMap;
    }
    
    public String analyzeCode(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "❌ Archivo no encontrado: " + filePath;
            }
            
            List<PolicyViolation> violations = new ArrayList<>();
            analyzeFile(path, violations);
            
            return formatPolicyResults(violations, filePath);
            
        } catch (Exception e) {
            return "❌ Error analizando código: " + e.getMessage();
        }
    }
    
    public String analyzeRepository(String repoPath) {
        try {
            Path path = Paths.get(repoPath);
            if (!Files.exists(path)) {
                return "❌ Repositorio no encontrado: " + repoPath;
            }
            
            List<PolicyViolation> violations = new ArrayList<>();
            analyzeDirectory(path, violations);
            
            return formatPolicyResults(violations, repoPath);
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    private void analyzeDirectory(Path dir, List<PolicyViolation> violations) throws IOException {
        Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(this::isSupportedFile)
            .forEach(file -> analyzeFile(file, violations));
    }
    
    private boolean isSupportedFile(Path file) {
        String name = file.toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts");
    }
    
    private void analyzeFile(Path file, List<PolicyViolation> violations) {
        try {
            String content = new String(Files.readAllBytes(file));
            String extension = getFileExtension(file);
            List<PolicyRule> fileRules = rules.get(extension);
            if (fileRules == null) return;
            
            String[] lines = content.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                for (PolicyRule rule : fileRules) {
                    if (Pattern.compile(rule.pattern).matcher(lines[i]).find()) {
                        violations.add(new PolicyViolation(
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
    
    private String formatPolicyResults(List<PolicyViolation> violations, String path) {
        if (violations.isEmpty()) {
            return "✅ Código cumple con las políticas de desarrollo\\n📁 " + path;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("📋 ANÁLISIS DE POLÍTICAS DE DESARROLLO\\n");
        result.append("📁 ").append(path).append("\\n");
        result.append("📊 Violaciones encontradas: ").append(violations.size()).append("\\n\\n");
        
        Map<String, Integer> severityCount = new HashMap<>();
        for (PolicyViolation violation : violations) {
            severityCount.merge(violation.severity, 1, Integer::sum);
            result.append(formatViolation(violation)).append("\\n");
        }
        
        result.append("\\n📈 RESUMEN:\\n");
        severityCount.forEach((severity, count) -> 
            result.append(getSeverityIcon(severity)).append(" ").append(severity).append(": ").append(count).append("\\n"));
        
        return result.toString();
    }
    
    private String formatViolation(PolicyViolation violation) {
        return String.format("%s %s\\n📁 %s:%d\\n💻 %s\\n🔧 %s\\n",
            getSeverityIcon(violation.severity), violation.name,
            violation.file.substring(violation.file.lastIndexOf('\\') + 1), violation.line,
            violation.code,
            violation.solution);
    }
    
    private String getSeverityIcon(String severity) {
        switch (severity) {
            case "CRÍTICO": return "🔴";
            case "ALTO": return "🟠";
            case "MEDIO": return "🟡";
            default: return "🔵";
        }
    }
    
    static class PolicyRule {
        final String name, pattern, severity, solution;
        
        PolicyRule(String name, String pattern, String severity, String solution) {
            this.name = name;
            this.pattern = pattern;
            this.severity = severity;
            this.solution = solution;
        }
    }
    
    static class PolicyViolation {
        final String name, severity, solution, file, code;
        final int line;
        
        PolicyViolation(String name, String severity, String solution, 
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