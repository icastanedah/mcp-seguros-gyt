package org.example;

import java.util.*;

public class SimpleAnalysisProcessor {
    
    public static void processSimpleAnalysis(String analysisResult) {
        if (analysisResult == null || analysisResult.isEmpty()) {
            System.out.println("⚠️ No hay resultados para procesar");
            return;
        }
        
        String[] lines = analysisResult.split("\n");
        
        // Contar violaciones por tipo
        Map<String, Integer> violationCounts = new HashMap<>();
        Map<String, Integer> fileCounts = new HashMap<>();
        
        for (String line : lines) {
            if (line.contains("HARDCODED_STRING")) {
                violationCounts.merge("HARDCODED_STRING", 1, Integer::sum);
            }
            if (line.contains("JAVADOC_PUBLICO")) {
                violationCounts.merge("JAVADOC_PUBLICO", 1, Integer::sum);
            }
            if (line.contains("METODO_PUBLICO_JAVADOC")) {
                violationCounts.merge("METODO_PUBLICO_JAVADOC", 1, Integer::sum);
            }
            if (line.contains("METODO_CAMELCASE")) {
                violationCounts.merge("METODO_CAMELCASE", 1, Integer::sum);
            }
            if (line.contains("VARIABLE_CAMELCASE")) {
                violationCounts.merge("VARIABLE_CAMELCASE", 1, Integer::sum);
            }
            if (line.contains("EXCEPTION_GENERICA")) {
                violationCounts.merge("EXCEPTION_GENERICA", 1, Integer::sum);
            }
            if (line.contains("NUMERO_MAGICO")) {
                violationCounts.merge("NUMERO_MAGICO", 1, Integer::sum);
            }
            if (line.contains("IMPORT_WILDCARD")) {
                violationCounts.merge("IMPORT_WILDCARD", 1, Integer::sum);
            }
            if (line.contains("CONSTANTE_UPPERCASE")) {
                violationCounts.merge("CONSTANTE_UPPERCASE", 1, Integer::sum);
            }
            
            // Contar archivos problemáticos
            if (line.contains(".java:")) {
                String[] parts = line.split(".java:");
                if (parts.length > 0) {
                    String fileName = parts[0].trim();
                    if (fileName.startsWith("? ")) {
                        fileName = fileName.substring(2);
                    }
                    fileName = fileName + ".java";
                    fileCounts.merge(fileName, 1, Integer::sum);
                }
            }
        }
        
        System.out.println("\n📊 ANÁLISIS SIMPLIFICADO DE VIOLACIONES");
        System.out.println("═══════════════════════════════════════════");
        
        int totalViolations = violationCounts.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total de violaciones detectadas: " + totalViolations);
        
        System.out.println("\n🔍 VIOLACIONES POR TIPO:");
        System.out.println("─────────────────────────────");
        violationCounts.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .forEach(entry -> {
                String priority = getPriority(entry.getKey());
                System.out.printf("%-25s: %3d %s\n", 
                    entry.getKey(), entry.getValue(), priority);
            });
        
        System.out.println("\n📁 TOP 10 ARCHIVOS MÁS PROBLEMÁTICOS:");
        System.out.println("─────────────────────────────────────");
        fileCounts.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(10)
            .forEach(entry -> {
                System.out.printf("%-50s: %3d violaciones\n", 
                    entry.getKey(), entry.getValue());
            });
        
        System.out.println("\n⚡ RECOMENDACIONES PRIORITARIAS:");
        System.out.println("─────────────────────────────────");
        
        if (violationCounts.getOrDefault("EXCEPTION_GENERICA", 0) > 0) {
            System.out.println("🔴 CRÍTICO: " + violationCounts.get("EXCEPTION_GENERICA") + 
                " capturas de Exception genérica - Reemplazar por excepciones específicas");
        }
        
        if (violationCounts.getOrDefault("NUMERO_MAGICO", 0) > 0) {
            System.out.println("🔴 CRÍTICO: " + violationCounts.get("NUMERO_MAGICO") + 
                " números mágicos - Definir constantes nombradas");
        }
        
        if (violationCounts.getOrDefault("HARDCODED_STRING", 0) > 0) {
            System.out.println("🟡 MEDIO: " + violationCounts.get("HARDCODED_STRING") + 
                " strings hardcodeados - Externalizar a properties");
        }
        
        int javadocTotal = violationCounts.getOrDefault("JAVADOC_PUBLICO", 0) + 
                          violationCounts.getOrDefault("METODO_PUBLICO_JAVADOC", 0);
        if (javadocTotal > 0) {
            System.out.println("🟢 BAJO: " + javadocTotal + 
                " elementos sin JavaDoc - Agregar documentación");
        }
        
        System.out.println("\n💡 PRÓXIMOS PASOS:");
        System.out.println("─────────────────");
        System.out.println("1. Configurar SonarQube para análisis continuo");
        System.out.println("2. Implementar pre-commit hooks");
        System.out.println("3. Establecer revisiones de código obligatorias");
        System.out.println("4. Crear guías de estilo para el equipo");
    }
    
    private static String getPriority(String violationType) {
        return switch (violationType) {
            case "EXCEPTION_GENERICA", "NUMERO_MAGICO" -> "🔴";
            case "HARDCODED_STRING", "METODO_CAMELCASE", "VARIABLE_CAMELCASE" -> "🟡";
            default -> "🟢";
        };
    }
}