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
            // Validar input usando la clase Config
            if (!Config.isPathSafe(filePath)) {
                return "❌ Error: Path del archivo no es seguro o no existe: " + filePath;
            }
            
            Path path = Paths.get(filePath).normalize();
            
            // Validar que el archivo existe y es accesible
            if (!Files.exists(path)) {
                return "❌ Archivo no encontrado: " + filePath;
            }
            
            if (!Files.isReadable(path)) {
                return "❌ No se puede leer el archivo: " + filePath;
            }
            
            List<PolicyViolation> violations = new ArrayList<>();
            analyzeFile(path, violations);
            
            return formatPolicyResults(violations, filePath);
            
        } catch (SecurityException e) {
            return "❌ Error de seguridad: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Error analizando código: " + e.getMessage();
        }
    }
    
    public String analyzeRepository(String repoPath) {
        try {
            // Resolver el path completo
            Path path = resolvePath(repoPath);
            
            // Validar que el repositorio existe y es accesible
            if (!Files.exists(path)) {
                return "❌ Repositorio no encontrado: " + repoPath + " (ruta completa: " + path + ")\n" +
                       "💡 Intenta usar 'auto' para búsqueda automática o verifica la ruta.";
            }
            
            if (!Files.isReadable(path)) {
                return "❌ No se puede leer el repositorio: " + repoPath;
            }
            
            List<PolicyViolation> violations = new ArrayList<>();
            analyzeDirectory(path, violations);
            
            return formatPolicyResults(violations, repoPath);
            
        } catch (SecurityException e) {
            return "❌ Error de seguridad: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Error analizando código: " + e.getMessage();
        }
    }
    
    /**
     * Resuelve un path relativo o absoluto
     */
    private Path resolvePath(String repoPath) {
        Path path = Paths.get(repoPath).normalize();
        
        if (path.isAbsolute()) {
            return path;
        }
        
        // Si el path comienza con "Documents/", intentar resolver desde el home del usuario
        if (repoPath.startsWith("Documents/") || repoPath.startsWith("Documents\\\\")) {
            String homeDir = System.getProperty("user.home");
            return Paths.get(homeDir).resolve(repoPath).normalize();
        }
        
        // Intentar resolver desde el directorio actual
        Path currentDir = Paths.get(Config.getWorkingDirectory());
        Path resolvedPath = currentDir.resolve(path).normalize();
        
        if (Files.exists(resolvedPath)) {
            return resolvedPath;
        }
        
        // Si no existe, devolver el path original
        return path;
    }
    
    private void analyzeDirectory(Path dir, List<PolicyViolation> violations) throws IOException {
        // Limitar la profundidad de búsqueda para evitar timeouts
        int maxDepth = 3;
        
        try (var stream = Files.walk(dir, maxDepth)) {
            stream.filter(this::isSupportedFile)
                  .filter(this::isReadableFile)
                  .limit(50) // Limitar el número de archivos para evitar timeouts
                  .forEach(file -> analyzeFile(file, violations));
        }
    }
    
    private boolean isSupportedFile(Path file) {
        String name = file.toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts");
    }
    
    private boolean isReadableFile(Path file) {
        try {
            return Files.isReadable(file) && Files.size(file) <= Config.getMaxFileSize();
        } catch (IOException e) {
            return false;
        }
    }
    
    private void analyzeFile(Path file, List<PolicyViolation> violations) {
        try {
            // Verificar tamaño del archivo
            long fileSize = Files.size(file);
            if (fileSize > Config.getMaxFileSize()) {
                violations.add(new PolicyViolation(
                    "FILE_TOO_LARGE", "MEDIO", "Archivo demasiado grande para analizar",
                    file.toString(), 0, "Tamaño: " + fileSize + " bytes"
                ));
                return;
            }
            
            String content = new String(Files.readAllBytes(file), java.nio.charset.Charset.forName(Config.getFileEncoding()));
            String extension = getFileExtension(file);
            List<PolicyRule> fileRules = rules.get(extension);
            if (fileRules == null) return;
            
            String[] lines = content.split("\\n");
            int maxLines = Math.min(lines.length, 1000); // Limitar el número de líneas para evitar timeouts
            
            for (int i = 0; i < maxLines; i++) {
                String line = lines[i];
                
                // Validar longitud de línea
                if (line.length() > Config.getMaxLineLength()) {
                    violations.add(new PolicyViolation(
                        "LINE_TOO_LONG", "BAJO", "Línea demasiado larga",
                        file.toString(), i + 1, "Longitud: " + line.length() + " caracteres"
                    ));
                    continue;
                }
                
                for (PolicyRule rule : fileRules) {
                    try {
                        if (Pattern.compile(rule.pattern).matcher(line).find()) {
                            violations.add(new PolicyViolation(
                                rule.name, rule.severity, rule.solution,
                                file.toString(), i + 1, line.trim()
                            ));
                            // Limitar el número de violations por archivo para evitar timeouts
                            if (violations.size() >= 20) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        // Ignorar patrones regex inválidos
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
            return "✅ Código cumple con las políticas de desarrollo\n📁 " + path;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("📋 ANÁLISIS DE POLÍTICAS DE DESARROLLO\n");
        result.append("📁 ").append(path).append("\n");
        result.append("📊 Violaciones encontradas: ").append(violations.size()).append("\n\n");
        
        Map<String, Integer> severityCount = new HashMap<>();
        for (PolicyViolation violation : violations) {
            severityCount.merge(violation.severity, 1, Integer::sum);
            result.append(formatViolation(violation)).append("\n");
        }
        
        result.append("\n📈 RESUMEN:\n");
        severityCount.forEach((severity, count) -> 
            result.append(getSeverityIcon(severity)).append(" ").append(severity).append(": ").append(count).append("\n"));
        
        return result.toString();
    }
    
    private String formatViolation(PolicyViolation violation) {
        String fileName = violation.file.substring(violation.file.lastIndexOf(Config.getFileSeparator()) + 1);
        return String.format("%s %s\n📁 %s:%d\n💻 %s\n🔧 %s\n",
            getSeverityIcon(violation.severity), violation.name,
            fileName, violation.line,
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