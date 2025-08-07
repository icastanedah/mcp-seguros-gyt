package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonReportGenerator {
    
    public static void generateJsonReport(String analysisResult, String outputPath) {
        try {
            AnalysisData data = parseAnalysisResult(analysisResult);
            String json = generateJsonContent(data);
            
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(json);
            }
            
            System.out.println("📊 Reporte JSON generado: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error generando reporte JSON: " + e.getMessage());
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
            parseViolationType(line, data, "HARDCODED_STRING");
            parseViolationType(line, data, "JAVADOC_PUBLICO");
            parseViolationType(line, data, "EXCEPTION_GENERICA");
            parseViolationType(line, data, "NUMERO_MAGICO");
            parseViolationType(line, data, "METODO_CAMELCASE");
            parseViolationType(line, data, "VARIABLE_CAMELCASE");
            parseViolationType(line, data, "IMPORT_WILDCARD");
            parseViolationType(line, data, "METODO_PUBLICO_JAVADOC");
            parseViolationType(line, data, "CONSTANTE_UPPERCASE");
            
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
    
    private static void parseViolationType(String line, AnalysisData data, String violationType) {
        if (line.contains(violationType)) {
            data.violationsByType.merge(violationType, 1, Integer::sum);
        }
    }
    
    private static String generateJsonContent(AnalysisData data) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"reportMetadata\": {\n");
        json.append("    \"generatedAt\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("    \"tool\": \"MCP Inspector\",\n");
        json.append("    \"version\": \"1.0\"\n");
        json.append("  },\n");
        
        json.append("  \"summary\": {\n");
        json.append("    \"totalViolations\": ").append(data.totalViolations).append(",\n");
        
        int highPriority = data.violationsByType.getOrDefault("EXCEPTION_GENERICA", 0) + 
                          data.violationsByType.getOrDefault("NUMERO_MAGICO", 0);
        int mediumPriority = data.violationsByType.getOrDefault("HARDCODED_STRING", 0) + 
                            data.violationsByType.getOrDefault("METODO_CAMELCASE", 0) + 
                            data.violationsByType.getOrDefault("VARIABLE_CAMELCASE", 0);
        int lowPriority = data.totalViolations - highPriority - mediumPriority;
        
        json.append("    \"priorityBreakdown\": {\n");
        json.append("      \"high\": ").append(highPriority).append(",\n");
        json.append("      \"medium\": ").append(mediumPriority).append(",\n");
        json.append("      \"low\": ").append(lowPriority).append("\n");
        json.append("    }\n");
        json.append("  },\n");
        
        json.append("  \"violationsByType\": {\n");
        List<Map.Entry<String, Integer>> sortedViolations = data.violationsByType.entrySet()
            .stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .collect(java.util.stream.Collectors.toList());
        
        for (int i = 0; i < sortedViolations.size(); i++) {
            Map.Entry<String, Integer> entry = sortedViolations.get(i);
            json.append("    \"").append(entry.getKey()).append("\": {\n");
            json.append("      \"count\": ").append(entry.getValue()).append(",\n");
            json.append("      \"priority\": \"").append(getViolationPriority(entry.getKey())).append("\",\n");
            json.append("      \"description\": \"").append(escapeJson(getViolationDescription(entry.getKey()))).append("\"\n");
            json.append("    }");
            if (i < sortedViolations.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  },\n");
        
        json.append("  \"topProblematicFiles\": [\n");
        List<Map.Entry<String, Integer>> sortedFiles = data.fileViolations.entrySet()
            .stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(15)
            .collect(java.util.stream.Collectors.toList());
        
        for (int i = 0; i < sortedFiles.size(); i++) {
            Map.Entry<String, Integer> entry = sortedFiles.get(i);
            json.append("    {\n");
            json.append("      \"fileName\": \"").append(escapeJson(entry.getKey())).append("\",\n");
            json.append("      \"violationCount\": ").append(entry.getValue()).append("\n");
            json.append("    }");
            if (i < sortedFiles.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        
        json.append("  \"recommendations\": [\n");
        json.append("    {\n");
        json.append("      \"phase\": \"Correcciones Críticas\",\n");
        json.append("      \"duration\": \"1-2 semanas\",\n");
        json.append("      \"priority\": \"high\",\n");
        json.append("      \"actions\": [\n");
        json.append("        \"Reemplazar capturas de Exception genérica\",\n");
        json.append("        \"Definir constantes para números mágicos\",\n");
        json.append("        \"Implementar manejo de errores granular\"\n");
        json.append("      ]\n");
        json.append("    },\n");
        json.append("    {\n");
        json.append("      \"phase\": \"Mejoras de Calidad\",\n");
        json.append("      \"duration\": \"2-3 semanas\",\n");
        json.append("      \"priority\": \"medium\",\n");
        json.append("      \"actions\": [\n");
        json.append("        \"Externalizar strings hardcodeados\",\n");
        json.append("        \"Corregir nomenclatura\",\n");
        json.append("        \"Implementar validaciones\"\n");
        json.append("      ]\n");
        json.append("    },\n");
        json.append("    {\n");
        json.append("      \"phase\": \"Documentación\",\n");
        json.append("      \"duration\": \"1-2 semanas\",\n");
        json.append("      \"priority\": \"low\",\n");
        json.append("      \"actions\": [\n");
        json.append("        \"Agregar JavaDoc\",\n");
        json.append("        \"Corregir imports\",\n");
        json.append("        \"Establecer guías de estilo\"\n");
        json.append("      ]\n");
        json.append("    }\n");
        json.append("  ]\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    private static String getViolationPriority(String type) {
        switch (type) {
            case "EXCEPTION_GENERICA":
            case "NUMERO_MAGICO":
                return "high";
            case "HARDCODED_STRING":
            case "METODO_CAMELCASE":
            case "VARIABLE_CAMELCASE":
                return "medium";
            default:
                return "low";
        }
    }
    
    private static String getViolationDescription(String type) {
        switch (type) {
            case "HARDCODED_STRING":
                return "Strings embebidos que deberían externalizarse";
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
    
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    static class AnalysisData {
        int totalViolations = 0;
        Map<String, Integer> violationsByType = new HashMap<>();
        Map<String, Integer> fileViolations = new HashMap<>();
    }
}