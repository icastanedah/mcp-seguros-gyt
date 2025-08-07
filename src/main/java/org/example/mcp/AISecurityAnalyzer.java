package org.example.mcp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class AISecurityAnalyzer {
    private final SecurityAnalyzer baseAnalyzer;
    private final AIAnalysisEngine aiEngine;
    
    public AISecurityAnalyzer() {
        this.baseAnalyzer = new SecurityAnalyzer();
        this.aiEngine = new AIAnalysisEngine();
    }
    
    public String analyzeRepository(String repoPath) {
        try {
            // 1. Análisis básico con reglas
            String basicResults = baseAnalyzer.scanRepository(repoPath);
            
            // 2. Análisis contextual con IA
            ProjectContext context = extractProjectContext(repoPath);
            
            // 3. Generar reporte inteligente
            return aiEngine.generateIntelligentReport(basicResults, context);
            
        } catch (Exception e) {
            return "❌ Error en análisis: " + e.getMessage();
        }
    }
    
    private ProjectContext extractProjectContext(String repoPath) throws IOException {
        Path path = Paths.get(repoPath);
        ProjectContext context = new ProjectContext();
        
        // Detectar tipo de proyecto
        context.projectType = detectProjectType(path);
        context.fileCount = countFiles(path);
        context.technologies = detectTechnologies(path);
        context.riskLevel = calculateRiskLevel(path);
        
        return context;
    }
    
    private String detectProjectType(Path path) throws IOException {
        if (Files.exists(path.resolve("pom.xml"))) return "Maven Java";
        if (Files.exists(path.resolve("build.gradle"))) return "Gradle Java";
        if (Files.exists(path.resolve("package.json"))) return "Node.js";
        if (Files.exists(path.resolve("requirements.txt"))) return "Python";
        return "Desconocido";
    }
    
    private int countFiles(Path path) throws IOException {
        return (int) Files.walk(path)
            .filter(Files::isRegularFile)
            .filter(p -> !p.toString().contains(".git"))
            .count();
    }
    
    private List<String> detectTechnologies(Path path) throws IOException {
        Set<String> techs = new HashSet<>();
        
        Files.walk(path)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                String name = file.toString().toLowerCase();
                if (name.endsWith(".java")) techs.add("Java");
                if (name.endsWith(".js") || name.endsWith(".ts")) techs.add("JavaScript/TypeScript");
                if (name.endsWith(".py")) techs.add("Python");
                if (name.endsWith(".sql")) techs.add("SQL");
                if (name.contains("docker")) techs.add("Docker");
            });
            
        return new ArrayList<>(techs);
    }
    
    private String calculateRiskLevel(Path path) throws IOException {
        long criticalFiles = Files.walk(path)
            .filter(Files::isRegularFile)
            .filter(p -> {
                String name = p.toString().toLowerCase();
                return name.contains("password") || name.contains("secret") || 
                       name.contains("config") || name.contains("auth");
            })
            .count();
            
        if (criticalFiles > 10) return "ALTO";
        if (criticalFiles > 5) return "MEDIO";
        return "BAJO";
    }
    
    static class ProjectContext {
        String projectType;
        int fileCount;
        List<String> technologies;
        String riskLevel;
    }
    
    static class AIAnalysisEngine {
        public String generateIntelligentReport(String basicResults, ProjectContext context) {
            StringBuilder report = new StringBuilder();
            
            // Header inteligente
            report.append("🤖 ANÁLISIS INTELIGENTE DE SEGURIDAD\n");
            report.append("═══════════════════════════════════════\n\n");
            
            // Resumen ejecutivo
            report.append("📋 RESUMEN EJECUTIVO\n");
            report.append("├─ Tipo de proyecto: ").append(context.projectType).append("\n");
            report.append("├─ Archivos analizados: ").append(context.fileCount).append("\n");
            report.append("├─ Tecnologías: ").append(String.join(", ", context.technologies)).append("\n");
            report.append("└─ Nivel de riesgo: ").append(getRiskIcon(context.riskLevel)).append(" ").append(context.riskLevel).append("\n\n");
            
            // Análisis de vulnerabilidades
            if (basicResults.contains("Issues encontrados:")) {
                report.append("🔍 VULNERABILIDADES DETECTADAS\n");
                report.append(formatVulnerabilities(basicResults));
            } else {
                report.append("✅ ESTADO DE SEGURIDAD: BUENO\n");
                report.append("No se detectaron vulnerabilidades críticas.\n\n");
            }
            
            // Recomendaciones inteligentes
            report.append(generateSmartRecommendations(context, basicResults));
            
            // Próximos pasos
            report.append("🎯 PRÓXIMOS PASOS\n");
            report.append("1. Revisar vulnerabilidades críticas primero\n");
            report.append("2. Implementar análisis automatizado en CI/CD\n");
            report.append("3. Configurar monitoreo continuo\n");
            
            return report.toString();
        }
        
        private String formatVulnerabilities(String basicResults) {
            // Extraer y formatear mejor las vulnerabilidades
            String[] lines = basicResults.split("\n");
            StringBuilder formatted = new StringBuilder();
            
            for (String line : lines) {
                if (line.contains("🔴") || line.contains("🟠") || line.contains("🟡")) {
                    formatted.append("├─ ").append(line).append("\n");
                }
            }
            
            return formatted.toString() + "\n";
        }
        
        private String generateSmartRecommendations(ProjectContext context, String results) {
            StringBuilder recs = new StringBuilder();
            recs.append("💡 RECOMENDACIONES INTELIGENTES\n");
            
            // Recomendaciones basadas en el tipo de proyecto
            if (context.projectType.contains("Java")) {
                recs.append("├─ Implementar SpotBugs para análisis estático\n");
                recs.append("├─ Usar OWASP Dependency Check\n");
            }
            
            if (context.technologies.contains("JavaScript/TypeScript")) {
                recs.append("├─ Configurar ESLint con reglas de seguridad\n");
                recs.append("├─ Usar npm audit regularmente\n");
            }
            
            // Recomendaciones basadas en el riesgo
            if (context.riskLevel.equals("ALTO")) {
                recs.append("├─ ⚠️  Realizar auditoría de seguridad completa\n");
                recs.append("├─ Implementar autenticación multifactor\n");
            }
            
            recs.append("└─ Establecer pipeline de seguridad automatizada\n\n");
            
            return recs.toString();
        }
        
        private String getRiskIcon(String risk) {
            switch (risk) {
                case "ALTO": return "🔴";
                case "MEDIO": return "🟡";
                default: return "🟢";
            }
        }
    }
}