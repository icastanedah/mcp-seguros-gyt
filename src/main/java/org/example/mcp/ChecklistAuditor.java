package org.example.mcp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class ChecklistAuditor {
    private final Map<String, List<ChecklistRule>> rules;
    
    public ChecklistAuditor() {
        this.rules = initializeChecklistRules();
    }
    
    private Map<String, List<ChecklistRule>> initializeChecklistRules() {
        Map<String, List<ChecklistRule>> ruleMap = new HashMap<>();
        
        // Reglas basadas en el checklist de desarrollo
        List<ChecklistRule> javaRules = new ArrayList<>();
        
        // Documentación y comentarios
        javaRules.add(new ChecklistRule("MISSING_CLASS_JAVADOC", "^\\s*public\\s+class\\s+\\w+", "DOCUMENTACIÓN", "Agregar JavaDoc a la clase"));
        javaRules.add(new ChecklistRule("MISSING_METHOD_JAVADOC", "^\\s*public\\s+\\w+.*\\(.*\\)\\s*\\{", "DOCUMENTACIÓN", "Agregar JavaDoc al método público"));
        
        // Manejo de errores
        javaRules.add(new ChecklistRule("EMPTY_CATCH", "catch\\s*\\([^)]+\\)\\s*\\{\\s*\\}", "MANEJO_ERRORES", "Implementar manejo adecuado de excepciones"));
        javaRules.add(new ChecklistRule("GENERIC_EXCEPTION", "throws\\s+Exception", "MANEJO_ERRORES", "Usar excepciones específicas en lugar de Exception genérica"));
        javaRules.add(new ChecklistRule("SYSTEM_OUT_PRINT", "System\\.out\\.print", "LOGGING", "Usar logger en lugar de System.out"));
        
        // Validaciones
        javaRules.add(new ChecklistRule("MISSING_NULL_CHECK", "\\w+\\.\\w+\\(", "VALIDACIÓN", "Verificar validación de parámetros null"));
        javaRules.add(new ChecklistRule("HARDCODED_VALUES", "\"[A-Z_]{3,}\"", "CONFIGURACIÓN", "Mover valores hardcodeados a configuración"));
        
        // Performance y recursos
        javaRules.add(new ChecklistRule("RESOURCE_LEAK", "new\\s+(FileInputStream|FileOutputStream|BufferedReader)", "RECURSOS", "Usar try-with-resources"));
        javaRules.add(new ChecklistRule("STRING_CONCATENATION", "\\+\\s*\"", "PERFORMANCE", "Considerar StringBuilder para concatenación múltiple"));
        
        // Testing
        javaRules.add(new ChecklistRule("MISSING_TESTS", "^\\s*public\\s+class\\s+(\\w+)(?!Test)", "TESTING", "Verificar que existe clase de test correspondiente"));
        
        ruleMap.put("java", javaRules);
        
        // Reglas para JavaScript/TypeScript
        List<ChecklistRule> jsRules = new ArrayList<>();
        jsRules.add(new ChecklistRule("MISSING_JSDoc", "^\\s*function\\s+\\w+", "DOCUMENTACIÓN", "Agregar JSDoc a la función"));
        jsRules.add(new ChecklistRule("CONSOLE_LOG_PROD", "console\\.log\\(", "LOGGING", "Remover console.log en producción"));
        jsRules.add(new ChecklistRule("VAR_USAGE", "\\bvar\\s+", "BUENAS_PRÁCTICAS", "Usar let/const en lugar de var"));
        jsRules.add(new ChecklistRule("MISSING_ERROR_HANDLING", "fetch\\(.*\\)(?!.*catch)", "MANEJO_ERRORES", "Agregar manejo de errores a fetch"));
        
        ruleMap.put("js", jsRules);
        ruleMap.put("ts", jsRules);
        
        return ruleMap;
    }
    
    public String auditProject(String projectPath) {
        try {
            Path path = Paths.get(projectPath);
            if (!Files.exists(path)) {
                return "❌ Proyecto no encontrado: " + projectPath;
            }
            
            List<ChecklistIssue> issues = new ArrayList<>();
            Map<String, Integer> categoryCount = new HashMap<>();
            
            scanDirectory(path, issues, categoryCount);
            
            return formatChecklistResults(issues, categoryCount, projectPath);
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    private void scanDirectory(Path dir, List<ChecklistIssue> issues, Map<String, Integer> categoryCount) throws IOException {
        Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(this::isSupportedFile)
            .forEach(file -> scanFileForChecklist(file, issues, categoryCount));
    }
    
    private boolean isSupportedFile(Path file) {
        String name = file.toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts") || name.endsWith(".jsx") || name.endsWith(".tsx");
    }
    
    private void scanFileForChecklist(Path file, List<ChecklistIssue> issues, Map<String, Integer> categoryCount) {
        try {
            String content = new String(Files.readAllBytes(file));
            String extension = getFileExtension(file);
            List<ChecklistRule> fileRules = rules.get(extension);
            if (fileRules == null) return;
            
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                for (ChecklistRule rule : fileRules) {
                    if (Pattern.compile(rule.pattern).matcher(lines[i]).find()) {
                        issues.add(new ChecklistIssue(
                            rule.name, rule.category, rule.recommendation,
                            file.toString(), i + 1, lines[i].trim()
                        ));
                        categoryCount.put(rule.category, categoryCount.getOrDefault(rule.category, 0) + 1);
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
    
    private String formatChecklistResults(List<ChecklistIssue> issues, Map<String, Integer> categoryCount, String projectPath) {
        StringBuilder result = new StringBuilder();
        result.append("📋 AUDITORÍA DE CHECKLIST DE DESARROLLO\n");
        result.append("📁 ").append(projectPath).append("\n");
        result.append("📊 Total de items encontrados: ").append(issues.size()).append("\n\n");
        
        // Resumen por categoría
        result.append("📈 RESUMEN POR CATEGORÍA:\n");
        categoryCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> 
                result.append("  ").append(getCategoryIcon(entry.getKey()))
                      .append(" ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        
        result.append("\n🔍 DETALLES:\n");
        
        // Agrupar por categoría
        Map<String, List<ChecklistIssue>> groupedIssues = new HashMap<>();
        for (ChecklistIssue issue : issues) {
            groupedIssues.computeIfAbsent(issue.category, k -> new ArrayList<>()).add(issue);
        }
        
        groupedIssues.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                result.append("\n").append(getCategoryIcon(entry.getKey())).append(" ").append(entry.getKey()).append(":\n");
                entry.getValue().stream()
                    .limit(5) // Limitar a 5 por categoría para no saturar
                    .forEach(issue -> result.append(formatChecklistIssue(issue)).append("\n"));
                
                if (entry.getValue().size() > 5) {
                    result.append("  ... y ").append(entry.getValue().size() - 5).append(" más\n");
                }
            });
        
        return result.toString();
    }
    
    private String formatChecklistIssue(ChecklistIssue issue) {
        return String.format("  📄 %s:%d\n  💻 %s\n  💡 %s",
            issue.file.substring(issue.file.lastIndexOf('\\') + 1), issue.line,
            issue.code.length() > 80 ? issue.code.substring(0, 80) + "..." : issue.code,
            issue.recommendation);
    }
    
    private String getCategoryIcon(String category) {
        switch (category) {
            case "DOCUMENTACIÓN": return "📚";
            case "MANEJO_ERRORES": return "⚠️";
            case "LOGGING": return "📝";
            case "VALIDACIÓN": return "✅";
            case "CONFIGURACIÓN": return "⚙️";
            case "RECURSOS": return "🔧";
            case "PERFORMANCE": return "⚡";
            case "TESTING": return "🧪";
            case "BUENAS_PRÁCTICAS": return "👍";
            default: return "📌";
        }
    }
    
    static class ChecklistRule {
        final String name, pattern, category, recommendation;
        
        ChecklistRule(String name, String pattern, String category, String recommendation) {
            this.name = name;
            this.pattern = pattern;
            this.category = category;
            this.recommendation = recommendation;
        }
    }
    
    static class ChecklistIssue {
        final String name, category, recommendation, file, code;
        final int line;
        
        ChecklistIssue(String name, String category, String recommendation, 
                      String file, int line, String code) {
            this.name = name;
            this.category = category;
            this.recommendation = recommendation;
            this.file = file;
            this.line = line;
            this.code = code;
        }
    }
}