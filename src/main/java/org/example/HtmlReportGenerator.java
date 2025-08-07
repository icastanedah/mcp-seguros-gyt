package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HtmlReportGenerator {
    
    public static void generateHtmlReport(String analysisResult, String outputPath) {
        try {
            List<PolicyAnalysisProcessor.Violation> violations = parseViolations(analysisResult);
            
            StringBuilder html = new StringBuilder();
            html.append(generateHtmlHeader());
            html.append(generateSummarySection(violations));
            html.append(generateViolationsByTypeSection(violations));
            html.append(generateTopFilesSection(violations));
            html.append(generateDetailedViolationsSection(violations));
            html.append(generateRecommendationsSection());
            html.append(generateHtmlFooter());
            
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(html.toString());
            }
            
            System.out.println("📄 Reporte HTML generado: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error generando reporte HTML: " + e.getMessage());
        }
    }
    
    private static String generateHtmlHeader() {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reporte de Análisis de Políticas de Desarrollo</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px 10px 0 0; }
                    .header h1 { margin: 0; font-size: 2.5em; text-align: center; }
                    .header .subtitle { text-align: center; margin-top: 10px; opacity: 0.9; }
                    .section { padding: 30px; border-bottom: 1px solid #eee; }
                    .section:last-child { border-bottom: none; }
                    .section h2 { color: #333; border-bottom: 3px solid #667eea; padding-bottom: 10px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
                    .stat-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #667eea; }
                    .stat-number { font-size: 2em; font-weight: bold; color: #667eea; }
                    .stat-label { color: #666; margin-top: 5px; }
                    .violation-type { margin: 15px 0; padding: 15px; border-radius: 8px; }
                    .high-priority { background: #ffebee; border-left: 4px solid #f44336; }
                    .medium-priority { background: #fff3e0; border-left: 4px solid #ff9800; }
                    .low-priority { background: #e8f5e8; border-left: 4px solid #4caf50; }
                    .file-list { list-style: none; padding: 0; }
                    .file-item { background: #f8f9fa; margin: 10px 0; padding: 15px; border-radius: 8px; display: flex; justify-content: space-between; }
                    .violation-detail { background: #f8f9fa; margin: 10px 0; padding: 15px; border-radius: 8px; }
                    .code-snippet { background: #2d3748; color: #e2e8f0; padding: 15px; border-radius: 8px; font-family: 'Courier New', monospace; overflow-x: auto; }
                    .recommendation { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #2196f3; }
                    .footer { text-align: center; padding: 20px; color: #666; background: #f8f9fa; border-radius: 0 0 10px 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🤖 Análisis de Políticas de Desarrollo</h1>
                        <div class="subtitle">Reporte generado el """ + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + """
                        </div>
                    </div>
            """;
    }
    
    private static String generateSummarySection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, Integer> priorityCount = categorizePriorities(violations);
        
        return """
            <div class="section">
                <h2>📊 Resumen Ejecutivo</h2>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-number">""" + violations.size() + """
                        </div>
                        <div class="stat-label">Total Violaciones</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">""" + priorityCount.getOrDefault("HIGH", 0) + """
                        </div>
                        <div class="stat-label">Alta Prioridad</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">""" + priorityCount.getOrDefault("MEDIUM", 0) + """
                        </div>
                        <div class="stat-label">Media Prioridad</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">""" + priorityCount.getOrDefault("LOW", 0) + """
                        </div>
                        <div class="stat-label">Baja Prioridad</div>
                    </div>
                </div>
            </div>
            """;
    }
    
    private static String generateViolationsByTypeSection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, List<PolicyAnalysisProcessor.Violation>> violationsByType = new HashMap<>();
        for (PolicyAnalysisProcessor.Violation v : violations) {
            violationsByType.computeIfAbsent(v.getType(), k -> new ArrayList<>()).add(v);
        }
        
        StringBuilder section = new StringBuilder();
        section.append("""
            <div class="section">
                <h2>🔍 Violaciones por Tipo</h2>
            """);
        
        violationsByType.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .forEach(entry -> {
                String priority = getPriorityClass(entry.getKey());
                section.append(String.format("""
                    <div class="violation-type %s">
                        <strong>%s</strong> - %d violaciones
                        <div style="margin-top: 10px; color: #666;">%s</div>
                    </div>
                    """, priority, entry.getKey(), entry.getValue().size(), 
                    getViolationDescription(entry.getKey())));
            });
        
        section.append("</div>");
        return section.toString();
    }
    
    private static String generateTopFilesSection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, Integer> fileViolationCount = new HashMap<>();
        for (PolicyAnalysisProcessor.Violation v : violations) {
            fileViolationCount.merge(v.getFile(), 1, Integer::sum);
        }
        
        StringBuilder section = new StringBuilder();
        section.append("""
            <div class="section">
                <h2>📁 Archivos Más Problemáticos</h2>
                <ul class="file-list">
            """);
        
        fileViolationCount.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(10)
            .forEach(entry -> {
                section.append(String.format("""
                    <li class="file-item">
                        <span>%s</span>
                        <span><strong>%d violaciones</strong></span>
                    </li>
                    """, entry.getKey(), entry.getValue()));
            });
        
        section.append("</ul></div>");
        return section.toString();
    }
    
    private static String generateDetailedViolationsSection(List<PolicyAnalysisProcessor.Violation> violations) {
        StringBuilder section = new StringBuilder();
        section.append("""
            <div class="section">
                <h2>📋 Detalle de Violaciones (Top 20)</h2>
            """);
        
        violations.stream()
            .limit(20)
            .forEach(v -> {
                section.append(String.format("""
                    <div class="violation-detail">
                        <strong>%s</strong> en <em>%s:%d</em>
                        <div class="code-snippet">%s</div>
                        <div style="margin-top: 10px; color: #666;">%s</div>
                    </div>
                    """, v.getType(), v.getFile(), v.getLine(), 
                    escapeHtml(v.getCode()), v.getDescription()));
            });
        
        section.append("</div>");
        return section.toString();
    }
    
    private static String generateRecommendationsSection() {
        return """
            <div class="section">
                <h2>💡 Recomendaciones</h2>
                <div class="recommendation">
                    <h3>🔴 Alta Prioridad</h3>
                    <ul>
                        <li>Reemplazar capturas de Exception genérica por excepciones específicas</li>
                        <li>Definir constantes para números mágicos</li>
                        <li>Implementar manejo de errores más específico</li>
                    </ul>
                </div>
                <div class="recommendation">
                    <h3>🟡 Media Prioridad</h3>
                    <ul>
                        <li>Externalizar strings hardcodeados a archivos de propiedades</li>
                        <li>Corregir nomenclatura de variables y métodos</li>
                        <li>Implementar validaciones de entrada</li>
                    </ul>
                </div>
                <div class="recommendation">
                    <h3>🟢 Baja Prioridad</h3>
                    <ul>
                        <li>Agregar documentación JavaDoc a clases y métodos públicos</li>
                        <li>Reemplazar imports con wildcard por imports específicos</li>
                        <li>Mejorar formato y estilo del código</li>
                    </ul>
                </div>
            </div>
            """;
    }
    
    private static String generateHtmlFooter() {
        return """
                <div class="footer">
                    <p>Reporte generado por el Analizador de Políticas de Desarrollo</p>
                    <p>Para más información sobre las soluciones, consulte la documentación del proyecto</p>
                </div>
            </div>
            </body>
            </html>
            """;
    }
    
    // Métodos auxiliares
    private static List<PolicyAnalysisProcessor.Violation> parseViolations(String analysisResult) {
        // Implementación simplificada - en producción usar el parser completo
        return new ArrayList<>();
    }
    
    private static Map<String, Integer> categorizePriorities(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("HIGH", 0);
        priorities.put("MEDIUM", 0);
        priorities.put("LOW", 0);
        
        for (PolicyAnalysisProcessor.Violation v : violations) {
            String priority = getViolationPriority(v.getType());
            priorities.merge(priority, 1, Integer::sum);
        }
        
        return priorities;
    }
    
    private static String getViolationPriority(String type) {
        return switch (type) {
            case "EXCEPTION_GENERICA", "NUMERO_MAGICO" -> "HIGH";
            case "HARDCODED_STRING", "METODO_CAMELCASE", "VARIABLE_CAMELCASE" -> "MEDIUM";
            default -> "LOW";
        };
    }
    
    private static String getPriorityClass(String type) {
        return switch (getViolationPriority(type)) {
            case "HIGH" -> "high-priority";
            case "MEDIUM" -> "medium-priority";
            default -> "low-priority";
        };
    }
    
    private static String getViolationDescription(String type) {
        return switch (type) {
            case "HARDCODED_STRING" -> "Strings embebidos en código que deberían externalizarse";
            case "JAVADOC_PUBLICO" -> "Clases públicas sin documentación JavaDoc";
            case "METODO_PUBLICO_JAVADOC" -> "Métodos públicos sin documentación JavaDoc";
            case "EXCEPTION_GENERICA" -> "Captura de Exception genérica en lugar de específica";
            case "NUMERO_MAGICO" -> "Números literales sin contexto o constante";
            case "METODO_CAMELCASE" -> "Nomenclatura incorrecta en métodos";
            case "VARIABLE_CAMELCASE" -> "Nomenclatura incorrecta en variables";
            case "IMPORT_WILDCARD" -> "Imports con asterisco que deberían ser específicos";
            case "CONSTANTE_UPPERCASE" -> "Constantes que no siguen convención UPPER_CASE";
            default -> "Violación de política de desarrollo";
        };
    }
    
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}