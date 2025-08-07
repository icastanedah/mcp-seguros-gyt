package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyAnalysisProcessor {
    
    public static class Violation {
        private String type;
        private String file;
        private int line;
        private String code;
        private String description;
        
        public Violation(String type, String file, int line, String code, String description) {
            this.type = type;
            this.file = file;
            this.line = line;
            this.code = code;
            this.description = description;
        }
        
        // Getters
        public String getType() { return type; }
        public String getFile() { return file; }
        public int getLine() { return line; }
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
    
    public static void processAnalysisResult(String analysisResult) {
        List<Violation> violations = parseViolations(analysisResult);
        
        System.out.println("📊 RESUMEN DEL ANÁLISIS");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Total de violaciones encontradas: " + violations.size());
        
        // Agrupar por tipo de violación
        Map<String, List<Violation>> violationsByType = new HashMap<>();
        for (Violation v : violations) {
            violationsByType.computeIfAbsent(v.getType(), k -> new ArrayList<>()).add(v);
        }
        
        System.out.println("\n🔍 VIOLACIONES POR TIPO:");
        System.out.println("─────────────────────────────────────");
        violationsByType.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .forEach(entry -> {
                System.out.printf("%-25s: %3d violaciones\n", 
                    entry.getKey(), entry.getValue().size());
            });
        
        // Archivos más problemáticos
        Map<String, Integer> fileViolationCount = new HashMap<>();
        for (Violation v : violations) {
            fileViolationCount.merge(v.getFile(), 1, Integer::sum);
        }
        
        System.out.println("\n📁 ARCHIVOS MÁS PROBLEMÁTICOS:");
        System.out.println("─────────────────────────────────────");
        fileViolationCount.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(10)
            .forEach(entry -> {
                System.out.printf("%-40s: %3d violaciones\n", 
                    entry.getKey(), entry.getValue());
            });
        
        // Recomendaciones de prioridad
        System.out.println("\n⚡ RECOMENDACIONES DE PRIORIDAD:");
        System.out.println("─────────────────────────────────────");
        
        int hardcodedStrings = violationsByType.getOrDefault("HARDCODED_STRING", new ArrayList<>()).size();
        int javadocMissing = violationsByType.getOrDefault("JAVADOC_PUBLICO", new ArrayList<>()).size() +
                           violationsByType.getOrDefault("METODO_PUBLICO_JAVADOC", new ArrayList<>()).size();
        int namingIssues = violationsByType.getOrDefault("METODO_CAMELCASE", new ArrayList<>()).size() +
                          violationsByType.getOrDefault("VARIABLE_CAMELCASE", new ArrayList<>()).size();
        
        System.out.println("1. 🔴 ALTA PRIORIDAD:");
        System.out.println("   • Excepciones genéricas: " + 
            violationsByType.getOrDefault("EXCEPTION_GENERICA", new ArrayList<>()).size());
        System.out.println("   • Números mágicos: " + 
            violationsByType.getOrDefault("NUMERO_MAGICO", new ArrayList<>()).size());
        
        System.out.println("\n2. 🟡 MEDIA PRIORIDAD:");
        System.out.println("   • Strings hardcodeados: " + hardcodedStrings);
        System.out.println("   • Problemas de nomenclatura: " + namingIssues);
        
        System.out.println("\n3. 🟢 BAJA PRIORIDAD:");
        System.out.println("   • JavaDoc faltante: " + javadocMissing);
        System.out.println("   • Imports con wildcard: " + 
            violationsByType.getOrDefault("IMPORT_WILDCARD", new ArrayList<>()).size());
    }
    
    public static List<Violation> parseViolations(String analysisResult) {
        List<Violation> violations = new ArrayList<>();
        
        String[] lines = analysisResult.split("\n");
        
        for (int i = 0; i < lines.length - 3; i++) {
            String line = lines[i].trim();
            
            if (line.startsWith("? ") && line.length() > 2) {
                String type = line.substring(2).trim();
                
                if (i + 3 < lines.length) {
                    String fileLine = lines[i + 1].trim();
                    String codeLine = lines[i + 2].trim();
                    String descLine = lines[i + 3].trim();
                    
                    if (fileLine.startsWith("? ") && fileLine.contains(":")) {
                        String fileInfo = fileLine.substring(2).trim();
                        String[] parts = fileInfo.split(":");
                        if (parts.length >= 2) {
                            String file = parts[0];
                            int lineNum = 0;
                            try {
                                lineNum = Integer.parseInt(parts[1]);
                            } catch (NumberFormatException e) {
                                lineNum = 0;
                            }
                            
                            String code = codeLine.startsWith("? ") ? codeLine.substring(2) : codeLine;
                            String description = descLine.startsWith("? ") ? descLine.substring(2) : descLine;
                            
                            violations.add(new Violation(type, file, lineNum, code, description));
                        }
                    }
                }
            }
        }
        
        return violations;
    }
    
    public static void main(String[] args) {
        // Ejemplo de uso con el resultado del análisis
        // Usar el resultado real del análisis si está disponible
        String sampleResult = "Ejemplo de análisis procesado";
        
        System.out.println("🔧 PROCESADOR DE ANÁLISIS DE POLÍTICAS");
        System.out.println("═════════════════════════════════════════");
        System.out.println("Ejecute desde TestSecurity para ver el análisis completo.");
    }
}