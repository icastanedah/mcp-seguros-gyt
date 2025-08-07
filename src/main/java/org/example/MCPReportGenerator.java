package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MCPReportGenerator {
    
    public static void generateMCPReport(String analysisResult, String outputPath) {
        try {
            AnalysisData data = parseAnalysisResult(analysisResult);
            
            StringBuilder html = new StringBuilder();
            html.append(generateHeader());
            html.append(generateExecutiveSummary(data));
            html.append(generateViolationsByPriority(data));
            html.append(generateTopFiles(data));
            html.append(generateDetailedFindings(data));
            html.append(generateActionPlan());
            html.append(generateFooter());
            
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(html.toString());
            }
            
            System.out.println("📄 Reporte MCP generado: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error generando reporte MCP: " + e.getMessage());
        }
    }
    
    private static AnalysisData parseAnalysisResult(String result) {
        AnalysisData data = new AnalysisData();
        
        if (result == null || result.isEmpty()) {
            return data;
        }
        
        String[] lines = result.split("\n");
        
        for (String line : lines) {
            if (line.contains("Violaciones encontradas:")) {
                try {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        data.totalViolations = Integer.parseInt(parts[1].trim());
                    }
                } catch (NumberFormatException e) {
                    // Ignorar errores de parsing
                }
            }
            
            // Parsear violaciones específicas
            if (line.contains("HARDCODED_STRING")) {
                data.violationsByType.merge("HARDCODED_STRING", 1, Integer::sum);
            }
            if (line.contains("JAVADOC_PUBLICO")) {
                data.violationsByType.merge("JAVADOC_PUBLICO", 1, Integer::sum);
            }
            if (line.contains("EXCEPTION_GENERICA")) {
                data.violationsByType.merge("EXCEPTION_GENERICA", 1, Integer::sum);
            }
            if (line.contains("NUMERO_MAGICO")) {
                data.violationsByType.merge("NUMERO_MAGICO", 1, Integer::sum);
            }
            if (line.contains("METODO_CAMELCASE")) {
                data.violationsByType.merge("METODO_CAMELCASE", 1, Integer::sum);
            }
            if (line.contains("VARIABLE_CAMELCASE")) {
                data.violationsByType.merge("VARIABLE_CAMELCASE", 1, Integer::sum);
            }
            if (line.contains("IMPORT_WILDCARD")) {
                data.violationsByType.merge("IMPORT_WILDCARD", 1, Integer::sum);
            }
            if (line.contains("METODO_PUBLICO_JAVADOC")) {
                data.violationsByType.merge("METODO_PUBLICO_JAVADOC", 1, Integer::sum);
            }
            if (line.contains("CONSTANTE_UPPERCASE")) {
                data.violationsByType.merge("CONSTANTE_UPPERCASE", 1, Integer::sum);
            }
            
            // Parsear archivos problemáticos
            if (line.contains(".java:")) {
                String[] parts = line.split(".java:");
                if (parts.length > 0) {
                    String fileName = parts[0].trim();
                    if (fileName.startsWith("? ")) {
                        fileName = fileName.substring(2);
                    }
                    fileName = fileName + ".java";
                    data.fileViolations.merge(fileName, 1, Integer::sum);
                }
            }
        }
        
        return data;
    }
    
    private static String generateHeader() {
        return "<!DOCTYPE html>" +
            "<html lang=\"es\">" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Reporte MCP - Análisis de Políticas</title>" +
                "<style>" +
                    "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; }" +
                    ".container { max-width: 1400px; margin: 0 auto; padding: 20px; }" +
                    ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px; border-radius: 15px; text-align: center; margin-bottom: 30px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }" +
                    ".header h1 { font-size: 3em; margin-bottom: 10px; }" +
                    ".header .subtitle { font-size: 1.2em; opacity: 0.9; }" +
                    ".section { background: white; margin-bottom: 30px; border-radius: 15px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }" +
                    ".section h2 { color: #333; font-size: 1.8em; margin-bottom: 20px; border-bottom: 3px solid #667eea; padding-bottom: 10px; }" +
                    ".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }" +
                    ".stat-card { background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); padding: 25px; border-radius: 12px; text-align: center; border-left: 5px solid #667eea; transition: transform 0.3s; }" +
                    ".stat-card:hover { transform: translateY(-5px); }" +
                    ".stat-number { font-size: 2.5em; font-weight: bold; color: #667eea; margin-bottom: 5px; }" +
                    ".stat-label { color: #666; font-size: 1.1em; }" +
                    ".priority-high { background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%); border-left-color: #f44336; }" +
                    ".priority-medium { background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%); border-left-color: #ff9800; }" +
                    ".priority-low { background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%); border-left-color: #4caf50; }" +
                    ".violation-item { background: #f8f9fa; margin: 15px 0; padding: 20px; border-radius: 10px; border-left: 4px solid #667eea; }" +
                    ".file-list { list-style: none; }" +
                    ".file-item { background: #f8f9fa; margin: 10px 0; padding: 20px; border-radius: 10px; display: flex; justify-content: space-between; align-items: center; }" +
                    ".file-name { font-weight: 500; color: #333; }" +
                    ".violation-count { background: #667eea; color: white; padding: 5px 15px; border-radius: 20px; font-weight: bold; }" +
                    ".action-item { background: #e3f2fd; padding: 20px; margin: 15px 0; border-radius: 10px; border-left: 4px solid #2196f3; }" +
                    ".footer { text-align: center; padding: 30px; color: #666; background: white; border-radius: 15px; margin-top: 30px; }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"container\">" +
                    "<div class=\"header\">" +
                        "<h1>🤖 MCP Inspector Report</h1>" +
                        "<div class=\"subtitle\">Análisis de Políticas de Desarrollo - " + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                        "</div>" +
                    "</div>";
    }
    
    private static String generateExecutiveSummary(AnalysisData data) {
        int highPriority = data.violationsByType.getOrDefault("EXCEPTION_GENERICA", 0) + 
                          data.violationsByType.getOrDefault("NUMERO_MAGICO", 0);
        int mediumPriority = data.violationsByType.getOrDefault("HARDCODED_STRING", 0) + 
                            data.violationsByType.getOrDefault("METODO_CAMELCASE", 0) + 
                            data.violationsByType.getOrDefault("VARIABLE_CAMELCASE", 0);
        int lowPriority = data.totalViolations - highPriority - mediumPriority;
        
        return "<div class=\"section\">" +
                "<h2>📊 Resumen Ejecutivo</h2>" +
                "<div class=\"stats-grid\">" +
                    "<div class=\"stat-card\">" +
                        "<div class=\"stat-number\">" + data.totalViolations +
                        "</div>" +
                        "<div class=\"stat-label\">Total Violaciones</div>" +
                    "</div>" +
                    "<div class=\"stat-card priority-high\">" +
                        "<div class=\"stat-number\">" + highPriority +
                        "</div>" +
                        "<div class=\"stat-label\">🔴 Alta Prioridad</div>" +
                    "</div>" +
                    "<div class=\"stat-card priority-medium\">" +
                        "<div class=\"stat-number\">" + mediumPriority +
                        "</div>" +
                        "<div class=\"stat-label\">🟡 Media Prioridad</div>" +
                    "</div>" +
                    "<div class=\"stat-card priority-low\">" +
                        "<div class=\"stat-number\">" + lowPriority +
                        "</div>" +
                        "<div class=\"stat-label\">🟢 Baja Prioridad</div>" +
                    "</div>" +
                "</div>" +
            "</div>";
    }
    
    private static String generateViolationsByPriority(AnalysisData data) {
        StringBuilder section = new StringBuilder();
        section.append("<div class=\"section\">")
               .append("<h2>🔍 Violaciones por Tipo</h2>");
        
        data.violationsByType.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .forEach(entry -> {
                String description = getViolationDescription(entry.getKey());
                String priority = getPriorityIcon(entry.getKey());
                section.append("<div class=\"violation-item\">");
                section.append("<strong>").append(priority).append(" ").append(entry.getKey()).append("</strong> - ").append(entry.getValue()).append(" violaciones");
                section.append("<div style=\"margin-top: 10px; color: #666;\">").append(description).append("</div>");
                section.append("</div>");
            });
        
        section.append("</div>");
        return section.toString();
    }
    
    private static String generateTopFiles(AnalysisData data) {
        StringBuilder section = new StringBuilder();
        section.append("<div class=\"section\">")
               .append("<h2>📁 Archivos Más Problemáticos</h2>")
               .append("<ul class=\"file-list\">");
        
        data.fileViolations.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(15)
            .forEach(entry -> {
                section.append("<li class=\"file-item\">");
                section.append("<span class=\"file-name\">").append(entry.getKey()).append("</span>");
                section.append("<span class=\"violation-count\">").append(entry.getValue()).append("</span>");
                section.append("</li>");
            });
        
        section.append("</ul></div>");
        return section.toString();
    }
    
    private static String generateDetailedFindings(AnalysisData data) {
        return "<div class=\"section\">" +
            "<h2>🔬 Hallazgos Detallados</h2>" +
            "<div class=\"violation-item\">" +
                "<strong>🔴 Excepciones Genéricas</strong>" +
                "<p>Se encontraron " + data.violationsByType.getOrDefault("EXCEPTION_GENERICA", 0) + " casos de captura de Exception genérica. " +
                "Esto puede ocultar errores específicos y dificultar el debugging.</p>" +
            "</div>" +
            "<div class=\"violation-item\">" +
                "<strong>🔴 Números Mágicos</strong>" +
                "<p>Se detectaron " + data.violationsByType.getOrDefault("NUMERO_MAGICO", 0) + " números literales sin contexto. " +
                "Estos deben convertirse en constantes nombradas para mejorar la legibilidad.</p>" +
            "</div>" +
            "<div class=\"violation-item\">" +
                "<strong>🟡 Strings Hardcodeados</strong>" +
                "<p>Se encontraron " + data.violationsByType.getOrDefault("HARDCODED_STRING", 0) + " strings embebidos en código. " +
                "Considere externalizarlos a archivos de propiedades para facilitar la internacionalización.</p>" +
            "</div>" +
            "<div class=\"violation-item\">" +
                "<strong>🟢 Documentación Faltante</strong>" +
                "<p>Se detectaron " + (data.violationsByType.getOrDefault("JAVADOC_PUBLICO", 0) + 
                data.violationsByType.getOrDefault("METODO_PUBLICO_JAVADOC", 0)) + " elementos públicos sin JavaDoc. " +
                "La documentación mejora la mantenibilidad del código.</p>" +
            "</div>" +
        "</div>";
    }
    
    private static String generateActionPlan() {
        return "<div class=\"section\">" +
                "<h2>🎯 Plan de Acción Recomendado</h2>" +
                "<div class=\"action-item\">" +
                    "<h3>Fase 1: Correcciones Críticas (1-2 semanas)</h3>" +
                    "<ul>" +
                        "<li>Reemplazar todas las capturas de Exception genérica por excepciones específicas</li>" +
                        "<li>Definir constantes para todos los números mágicos identificados</li>" +
                        "<li>Implementar manejo de errores más granular</li>" +
                    "</ul>" +
                "</div>" +
                "<div class=\"action-item\">" +
                    "<h3>Fase 2: Mejoras de Calidad (2-3 semanas)</h3>" +
                    "<ul>" +
                        "<li>Externalizar strings hardcodeados a archivos de propiedades</li>" +
                        "<li>Corregir nomenclatura de variables y métodos</li>" +
                        "<li>Implementar validaciones de entrada robustas</li>" +
                    "</ul>" +
                "</div>" +
                "<div class=\"action-item\">" +
                    "<h3>Fase 3: Documentación y Estándares (1-2 semanas)</h3>" +
                    "<ul>" +
                        "<li>Agregar JavaDoc a todas las clases y métodos públicos</li>" +
                        "<li>Reemplazar imports con wildcard por imports específicos</li>" +
                        "<li>Establecer guías de estilo del equipo</li>" +
                    "</ul>" +
                "</div>" +
                "<div class=\"action-item\">" +
                    "<h3>Fase 4: Automatización (1 semana)</h3>" +
                    "<ul>" +
                        "<li>Configurar SonarQube para análisis continuo</li>" +
                        "<li>Implementar pre-commit hooks</li>" +
                        "<li>Establecer revisiones de código obligatorias</li>" +
                    "</ul>" +
                "</div>" +
            "</div>";
    }
    
    private static String generateFooter() {
        return "<div class=\"footer\">" +
                    "<p><strong>Reporte generado por MCP Inspector</strong></p>" +
                    "<p>Para más información sobre las soluciones específicas, consulte la documentación del proyecto</p>" +
                    "<p>Próxima revisión recomendada: " + 
                    LocalDateTime.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    "</p>" +
                "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
    
    private static String getViolationDescription(String type) {
        switch (type) {
            case "HARDCODED_STRING": return "Strings embebidos que deberían externalizarse a propiedades";
            case "JAVADOC_PUBLICO": return "Clases públicas sin documentación JavaDoc";
            case "METODO_PUBLICO_JAVADOC": return "Métodos públicos sin documentación JavaDoc";
            case "EXCEPTION_GENERICA": return "Captura de Exception genérica en lugar de específica";
            case "NUMERO_MAGICO": return "Números literales sin contexto o constante";
            case "METODO_CAMELCASE": return "Nomenclatura incorrecta en métodos";
            case "VARIABLE_CAMELCASE": return "Nomenclatura incorrecta en variables";
            case "IMPORT_WILDCARD": return "Imports con asterisco que deberían ser específicos";
            case "CONSTANTE_UPPERCASE": return "Constantes que no siguen convención UPPER_CASE";
            default: return "Violación de política de desarrollo";
        }
    }
    
    private static String getPriorityIcon(String type) {
        switch (type) {
            case "EXCEPTION_GENERICA":
            case "NUMERO_MAGICO": return "🔴";
            case "HARDCODED_STRING":
            case "METODO_CAMELCASE":
            case "VARIABLE_CAMELCASE": return "🟡";
            default: return "🟢";
        }
    }
    
    static class AnalysisData {
        int totalViolations = 0;
        Map<String, Integer> violationsByType = new HashMap<>();
        Map<String, Integer> fileViolations = new HashMap<>();
    }
}