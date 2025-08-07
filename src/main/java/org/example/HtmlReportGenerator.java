package org.example;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HtmlReportGenerator {
    
    public static void generateHtmlReport(String analysisResult, String outputPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            List<PolicyAnalysisProcessor.Violation> violations = parseViolations(analysisResult);
            
            // Generar el reporte HTML completo
            String htmlContent = generateHtmlHeader() +
                               generateSummarySection(violations) +
                               generateViolationsByTypeSection(violations) +
                               generateTopFilesSection(violations) +
                               generateDetailedViolationsSection(violations) +
                               generateRecommendationsSection() +
                               generateHtmlFooter();
            
            writer.write(htmlContent);
            System.out.println("✅ Reporte HTML generado exitosamente en: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error generando reporte HTML: " + e.getMessage());
        }
    }
    
    private static String generateHtmlHeader() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"es\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>Reporte de Análisis de Políticas de Desarrollo</title>\n" +
               "    <style>\n" +
               "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
               "        .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n" +
               "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px 10px 0 0; }\n" +
               "        .header h1 { margin: 0; font-size: 2.5em; text-align: center; }\n" +
               "        .header .subtitle { text-align: center; margin-top: 10px; opacity: 0.9; }\n" +
               "        .section { padding: 30px; border-bottom: 1px solid #eee; }\n" +
               "        .section:last-child { border-bottom: none; }\n" +
               "        .section h2 { color: #333; border-bottom: 3px solid #667eea; padding-bottom: 10px; }\n" +
               "        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }\n" +
               "        .stat-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #667eea; }\n" +
               "        .stat-number { font-size: 2em; font-weight: bold; color: #667eea; }\n" +
               "        .stat-label { color: #666; margin-top: 5px; }\n" +
               "        .violation-type { margin: 15px 0; padding: 15px; border-radius: 8px; }\n" +
               "        .high-priority { background: #ffebee; border-left: 4px solid #f44336; }\n" +
               "        .medium-priority { background: #fff3e0; border-left: 4px solid #ff9800; }\n" +
               "        .low-priority { background: #e8f5e8; border-left: 4px solid #4caf50; }\n" +
               "        .file-list { list-style: none; padding: 0; }\n" +
               "        .file-item { background: #f8f9fa; margin: 10px 0; padding: 15px; border-radius: 8px; display: flex; justify-content: space-between; }\n" +
               "        .violation-detail { background: #f8f9fa; margin: 10px 0; padding: 15px; border-radius: 8px; }\n" +
               "        .code-snippet { background: #2d3748; color: #e2e8f0; padding: 15px; border-radius: 8px; font-family: 'Courier New', monospace; overflow-x: auto; }\n" +
               "        .recommendation { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #2196f3; }\n" +
               "        .footer { text-align: center; padding: 20px; color: #666; background: #f8f9fa; border-radius: 0 0 10px 10px; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <div class=\"header\">\n" +
               "            <h1>🤖 Análisis de Políticas de Desarrollo</h1>\n" +
               "            <div class=\"subtitle\">Reporte generado el " + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + 
               "</div>\n" +
               "        </div>\n";
    }
    
    private static String generateSummarySection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, Integer> priorityCount = categorizePriorities(violations);
        
        return "<div class=\"section\">\n" +
               "    <h2>📊 Resumen Ejecutivo</h2>\n" +
               "    <div class=\"stats-grid\">\n" +
               "        <div class=\"stat-card\">\n" +
               "            <div class=\"stat-number\">" + violations.size() + "</div>\n" +
               "            <div class=\"stat-label\">Total Violaciones</div>\n" +
               "        </div>\n" +
               "        <div class=\"stat-card\">\n" +
               "            <div class=\"stat-number\">" + priorityCount.getOrDefault("HIGH", 0) + "</div>\n" +
               "            <div class=\"stat-label\">Alta Prioridad</div>\n" +
               "        </div>\n" +
               "        <div class=\"stat-card\">\n" +
               "            <div class=\"stat-number\">" + priorityCount.getOrDefault("MEDIUM", 0) + "</div>\n" +
               "            <div class=\"stat-label\">Media Prioridad</div>\n" +
               "        </div>\n" +
               "        <div class=\"stat-card\">\n" +
               "            <div class=\"stat-number\">" + priorityCount.getOrDefault("LOW", 0) + "</div>\n" +
               "            <div class=\"stat-label\">Baja Prioridad</div>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</div>\n";
    }
    
    private static String generateViolationsByTypeSection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, List<PolicyAnalysisProcessor.Violation>> violationsByType = new HashMap<>();
        for (PolicyAnalysisProcessor.Violation v : violations) {
            violationsByType.computeIfAbsent(v.getType(), k -> new ArrayList<>()).add(v);
        }
        
        StringBuilder section = new StringBuilder();
        section.append("<div class=\"section\">\n");
        section.append("    <h2>🔍 Violaciones por Tipo</h2>\n");
        
        violationsByType.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .forEach(entry -> {
                String priority = getPriorityClass(entry.getKey());
                section.append("<div class=\"violation-type ").append(priority).append("\">\n");
                section.append("    <strong>").append(entry.getKey()).append("</strong> - ").append(entry.getValue().size()).append(" violaciones\n");
                section.append("    <p>").append(getViolationDescription(entry.getKey())).append("</p>\n");
                section.append("</div>\n");
            });
        
        section.append("</div>\n");
        return section.toString();
    }
    
    private static String generateTopFilesSection(List<PolicyAnalysisProcessor.Violation> violations) {
        Map<String, Integer> fileViolations = new HashMap<>();
        for (PolicyAnalysisProcessor.Violation v : violations) {
            fileViolations.merge(v.getFile(), 1, Integer::sum);
        }
        
        StringBuilder section = new StringBuilder();
        section.append("<div class=\"section\">\n");
        section.append("    <h2>📁 Archivos con Más Violaciones</h2>\n");
        section.append("    <ul class=\"file-list\">\n");
        
        fileViolations.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(10)
            .forEach(entry -> {
                section.append("        <li class=\"file-item\">\n");
                section.append("            <span>").append(entry.getKey()).append("</span>\n");
                section.append("            <span><strong>").append(entry.getValue()).append(" violaciones</strong></span>\n");
                section.append("        </li>\n");
            });
        
        section.append("    </ul>\n");
        section.append("</div>\n");
        return section.toString();
    }
    
    private static String generateDetailedViolationsSection(List<PolicyAnalysisProcessor.Violation> violations) {
        StringBuilder section = new StringBuilder();
        section.append("<div class=\"section\">\n");
        section.append("    <h2>📋 Detalle de Violaciones (Top 20)</h2>\n");
        
        violations.stream()
            .limit(20)
            .forEach(v -> {
                section.append("        <div class=\"violation-detail\">\n");
                section.append("            <strong>").append(v.getType()).append("</strong> en <em>").append(v.getFile()).append(":").append(v.getLine()).append("</em>\n");
                section.append("            <div class=\"code-snippet\">").append(escapeHtml(v.getCode())).append("</div>\n");
                section.append("            <div style=\"margin-top: 10px; color: #666;\">").append(v.getDescription()).append("</div>\n");
                section.append("        </div>\n");
            });
        
        section.append("</div>\n");
        return section.toString();
    }
    
    private static String generateRecommendationsSection() {
        return "<div class=\"section\">\n" +
               "    <h2>💡 Recomendaciones</h2>\n" +
               "    <div class=\"recommendation\">\n" +
               "        <h3>🔴 Alta Prioridad</h3>\n" +
               "        <ul>\n" +
               "            <li>Reemplazar capturas de Exception genérica por excepciones específicas</li>\n" +
               "            <li>Definir constantes para números mágicos</li>\n" +
               "            <li>Implementar manejo de errores más específico</li>\n" +
               "        </ul>\n" +
               "    </div>\n" +
               "    <div class=\"recommendation\">\n" +
               "        <h3>🟡 Media Prioridad</h3>\n" +
               "        <ul>\n" +
               "            <li>Externalizar strings hardcodeados a archivos de propiedades</li>\n" +
               "            <li>Corregir nomenclatura de variables y métodos</li>\n" +
               "            <li>Implementar validaciones de entrada</li>\n" +
               "        </ul>\n" +
               "    </div>\n" +
               "    <div class=\"recommendation\">\n" +
               "        <h3>🟢 Baja Prioridad</h3>\n" +
               "        <ul>\n" +
               "            <li>Agregar documentación JavaDoc a clases y métodos públicos</li>\n" +
               "            <li>Reemplazar imports con wildcard por imports específicos</li>\n" +
               "            <li>Mejorar formato y estilo del código</li>\n" +
               "        </ul>\n" +
               "    </div>\n" +
               "</div>\n";
    }
    
    private static String generateHtmlFooter() {
        return "        <div class=\"footer\">\n" +
               "            <p>Reporte generado por el Analizador de Políticas de Desarrollo</p>\n" +
               "            <p>Para más información sobre las soluciones, consulte la documentación del proyecto</p>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>\n";
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
        switch (type) {
            case "EXCEPTION_GENERICA":
            case "NUMERO_MAGICO":
                return "HIGH";
            case "HARDCODED_STRING":
            case "METODO_CAMELCASE":
            case "VARIABLE_CAMELCASE":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
    
    private static String getPriorityClass(String type) {
        switch (getViolationPriority(type)) {
            case "HIGH":
                return "high-priority";
            case "MEDIUM":
                return "medium-priority";
            default:
                return "low-priority";
        }
    }
    
    private static String getViolationDescription(String type) {
        switch (type) {
            case "HARDCODED_STRING":
                return "Strings embebidos en código que deberían externalizarse";
            case "JAVADOC_PUBLICO":
                return "Clases públicas sin documentación JavaDoc";
            case "METODO_PUBLICO_JAVADOC":
                return "Métodos públicos sin documentación JavaDoc";
            case "EXCEPTION_GENERICA":
                return "Captura de Exception genérica en lugar de específica";
            case "NUMERO_MAGICO":
                return "Números literales sin contexto o constante";
            case "METODO_CAMELCASE":
                return "Nomenclatura incorrecta en métodos";
            case "VARIABLE_CAMELCASE":
                return "Nomenclatura incorrecta en variables";
            case "IMPORT_WILDCARD":
                return "Imports con asterisco que deberían ser específicos";
            case "CONSTANTE_UPPERCASE":
                return "Constantes que no siguen convención UPPER_CASE";
            default:
                return "Violación de política de desarrollo";
        }
    }
    
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}