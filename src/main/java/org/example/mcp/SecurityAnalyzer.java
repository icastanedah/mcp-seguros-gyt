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
        javaRules.add(new SecurityRule("COMMAND_INJECTION", "Runtime\\.getRuntime\\(\\)\\.exec", "CRÍTICO", "Evitar ejecución de comandos"));
        javaRules.add(new SecurityRule("FILE_PATH_TRAVERSAL", "\\.\\./", "ALTO", "Validar paths de entrada"));
        ruleMap.put("java", javaRules);
        
        List<SecurityRule> jsRules = new ArrayList<>();
        jsRules.add(new SecurityRule("XSS_INNERHTML", "innerHTML\\s*=", "CRÍTICO", "Usar textContent"));
        jsRules.add(new SecurityRule("EVAL_JS", "eval\\(", "ALTO", "Evitar eval()"));
        jsRules.add(new SecurityRule("CONSOLE_LOG", "console\\.log\\(", "BAJO", "Remover en producción"));
        jsRules.add(new SecurityRule("INSECURE_FETCH", "fetch\\(['\"]http://", "MEDIO", "Usar HTTPS"));
        ruleMap.put("js", jsRules);
        
        return ruleMap;
    }
    
    public String scanRepository(String repoPath) {
        try {
            // Si el path está vacío o es "auto", buscar automáticamente
            if (repoPath == null || repoPath.trim().isEmpty() || repoPath.equals("auto")) {
                return scanRepositoryAuto();
            }
            
            // Resolver el path completo
            Path path = resolvePath(repoPath);
            
            // Validar que el path existe y es accesible
            if (!Files.exists(path)) {
                return "❌ Repositorio no encontrado: " + repoPath + " (ruta completa: " + path + ")\n" +
                       "💡 Intenta usar 'auto' para búsqueda automática o verifica la ruta.";
            }
            
            if (!Files.isReadable(path)) {
                return "❌ No se puede leer el repositorio: " + repoPath;
            }
            
            List<SecurityIssue> issues = new ArrayList<>();
            scanDirectory(path, issues);
            
            return formatResults(issues, repoPath);
            
        } catch (SecurityException e) {
            return "❌ Error de seguridad: " + e.getMessage();
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    /**
     * Busca automáticamente repositorios en ubicaciones comunes
     */
    private String scanRepositoryAuto() {
        List<String> searchPaths = new ArrayList<>();
        String homeDir = System.getProperty("user.home");
        
        // Solo buscar en las ubicaciones más comunes para evitar timeouts
        searchPaths.add(homeDir + "/Documents");
        searchPaths.add(homeDir + "/Desktop");
        searchPaths.add(System.getProperty("user.dir")); // Directorio actual
        
        StringBuilder result = new StringBuilder();
        result.append("🔍 Buscando repositorios automáticamente...\n\n");
        
        int totalIssues = 0;
        int reposFound = 0;
        
        for (String searchPath : searchPaths) {
            Path path = Paths.get(searchPath);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                continue;
            }
            
            try {
                List<Path> repositories = findRepositories(path);
                for (Path repo : repositories) {
                    reposFound++;
                    result.append("📁 Analizando: ").append(repo.getFileName()).append("\n");
                    
                    List<SecurityIssue> issues = new ArrayList<>();
                    scanDirectory(repo, issues);
                    totalIssues += issues.size();
                    
                    if (issues.isEmpty()) {
                        result.append("   ✅ Sin problemas encontrados\n");
                    } else {
                        result.append("   ⚠️ ").append(issues.size()).append(" problemas encontrados\n");
                        for (SecurityIssue issue : issues) {
                            result.append("      ").append(formatIssue(issue));
                        }
                    }
                    result.append("\n");
                    
                    // Limitar el número de repositorios para evitar timeouts
                    if (reposFound >= 5) {
                        result.append("⚠️ Límite de repositorios alcanzado (5). Deteniendo búsqueda.\n");
                        break;
                    }
                }
            } catch (Exception e) {
                result.append("❌ Error analizando ").append(searchPath).append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        result.append("\n📊 RESUMEN:\n");
        result.append("   • Repositorios encontrados: ").append(reposFound).append("\n");
        result.append("   • Problemas totales: ").append(totalIssues).append("\n");
        
        return result.toString();
    }
    
    /**
     * Encuentra repositorios en un directorio dado
     */
    private List<Path> findRepositories(Path dir) throws IOException {
        List<Path> repositories = new ArrayList<>();
        
        try (var stream = Files.list(dir)) {
            for (Path item : stream.collect(java.util.stream.Collectors.toList())) {
                if (Files.isDirectory(item)) {
                    // Verificar si es un repositorio (tiene archivos de código o .git)
                    if (isRepository(item)) {
                        repositories.add(item);
                    }
                }
            }
        }
        
        return repositories;
    }
    
    /**
     * Verifica si un directorio es un repositorio
     */
    private boolean isRepository(Path dir) {
        try {
            // Verificar si tiene .git (repositorio Git)
            if (Files.exists(dir.resolve(".git"))) {
                return true;
            }
            
            // Verificar si tiene archivos de código
            try (var stream = Files.walk(dir, 2)) {
                return stream.anyMatch(file -> {
                    String name = file.toString().toLowerCase();
                    return name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts") || 
                           name.endsWith(".py") || name.endsWith(".cpp") || name.endsWith(".c") ||
                           name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".php");
                });
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private void scanDirectory(Path dir, List<SecurityIssue> issues) throws IOException {
        // Limitar la profundidad de búsqueda para evitar timeouts
        int maxDepth = 3;
        
        try (var stream = Files.walk(dir, maxDepth)) {
            stream.filter(this::isSupportedFile)
                  .filter(this::isReadableFile)
                  .limit(50) // Limitar el número de archivos para evitar timeouts
                  .forEach(file -> scanFile(file, issues));
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
    
    private void scanFile(Path file, List<SecurityIssue> issues) {
        try {
            // Verificar tamaño del archivo
            long fileSize = Files.size(file);
            if (fileSize > Config.getMaxFileSize()) {
                issues.add(new SecurityIssue(
                    "FILE_TOO_LARGE", "MEDIO", "Archivo demasiado grande para analizar",
                    file.toString(), 0, "Tamaño: " + fileSize + " bytes"
                ));
                return;
            }
            
            String content = new String(Files.readAllBytes(file), java.nio.charset.Charset.forName(Config.getFileEncoding()));
            String extension = getFileExtension(file);
            List<SecurityRule> fileRules = rules.get(extension);
            if (fileRules == null) return;
            
            String[] lines = content.split("\n");
            int maxLines = Math.min(lines.length, 1000); // Limitar el número de líneas para evitar timeouts
            
            for (int i = 0; i < maxLines; i++) {
                String line = lines[i];
                
                // Validar longitud de línea
                if (line.length() > Config.getMaxLineLength()) {
                    issues.add(new SecurityIssue(
                        "LINE_TOO_LONG", "BAJO", "Línea demasiado larga",
                        file.toString(), i + 1, "Longitud: " + line.length() + " caracteres"
                    ));
                    continue;
                }
                
                for (SecurityRule rule : fileRules) {
                    try {
                        if (Pattern.compile(rule.pattern).matcher(line).find()) {
                            issues.add(new SecurityIssue(
                                rule.name, rule.severity, rule.solution,
                                file.toString(), i + 1, line.trim()
                            ));
                            // Limitar el número de issues por archivo para evitar timeouts
                            if (issues.size() >= 20) {
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
        String fileName = issue.file.substring(issue.file.lastIndexOf(Config.getFileSeparator()) + 1);
        return String.format("%s %s\n📁 %s:%d\n💻 %s\n🔧 %s\n",
            getSeverityIcon(issue.severity), issue.name,
            fileName, issue.line,
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
    
    /**
     * Resuelve un path relativo o absoluto
     */
    private Path resolvePath(String repoPath) {
        Path path = Paths.get(repoPath).normalize();
        
        if (path.isAbsolute()) {
            return path;
        }
        
        // Si el path comienza con "Documents/", intentar resolver desde el home del usuario
        if (repoPath.startsWith("Documents/") || repoPath.startsWith("Documents\\")) {
            String homeDir = System.getProperty("user.home");
            return Paths.get(homeDir).resolve(repoPath).normalize();
        }
        
        // Intentar resolver desde el directorio actual
        Path currentDir = Paths.get(Config.getWorkingDirectory());
        Path resolvedPath = currentDir.resolve(path).normalize();
        
        if (Files.exists(resolvedPath)) {
            return resolvedPath;
        }
        
        // Si no existe, intentar desde el home del usuario
        String homeDir = System.getProperty("user.home");
        return Paths.get(homeDir).resolve(path).normalize();
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