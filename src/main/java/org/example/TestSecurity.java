package org.example;

import org.example.mcp.GeminiSecurityAnalyzer;

public class TestSecurity {
    public static void main(String[] args) {
        GeminiSecurityAnalyzer analyzer = new GeminiSecurityAnalyzer();
        
        System.out.println("┌────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│          🤖 GEMINI AI - ANALIZADOR DE POLÍTICAS DE DESARROLLO          │");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘\n");
        
        // Analizar el directorio del proyecto de cancelación de pólizas
        String targetPath = "C:\\Users\\icastaneda\\Documents\\policy-cancellation-management";
        
        System.out.println("📁 Directorio objetivo: " + targetPath);
        System.out.println("🔍 Iniciando análisis completo...\n");
        
        // Usar PolicyChecker para obtener resultados reales
        String result = PolicyChecker.checkPolicies(targetPath);
        
        // Si PolicyChecker no funciona, usar el analizador con IA
        if (result == null || result.contains("Error")) {
            System.out.println("⚠️ Usando analizador alternativo...");
            result = analyzer.analyzeWithAI(targetPath);
        }
        
        // Procesar y mostrar resultados estructurados
        if (result != null && !result.isEmpty()) {
            System.out.println("\n" + result);
            
            // Procesar el análisis para obtener estadísticas
            System.out.println("\n\n" + "=".repeat(80));
            SimpleAnalysisProcessor.processSimpleAnalysis(result);
            System.out.println("=".repeat(80));
            
            // Generar reportes completos
            try {
                String baseDir = "C:\\Users\\icastaneda\\Documents\\hackaton\\";
                
                // Reporte MCP principal (HTML mejorado)
                String mcpReportPath = baseDir + "mcp-analysis-report.html";
                MCPReportGenerator.generateMCPReport(result, mcpReportPath);
                System.out.println("📄 Reporte MCP HTML generado: " + mcpReportPath);
                
                // Reporte JSON para integraciones
                String jsonReportPath = baseDir + "mcp-analysis-report.json";
                JsonReportGenerator.generateJsonReport(result, jsonReportPath);
                System.out.println("📊 Reporte JSON generado: " + jsonReportPath);
                
                // Reporte HTML original como respaldo
                String htmlReportPath = baseDir + "policy-analysis-report.html";
                HtmlReportGenerator.generateHtmlReport(result, htmlReportPath);
                System.out.println("📄 Reporte HTML respaldo generado: " + htmlReportPath);
                
                // Abrir el reporte MCP automáticamente
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(mcpReportPath));
                    System.out.println("🌐 Abriendo reporte MCP en el navegador...");
                } catch (Exception openError) {
                    System.out.println("⚠️ No se pudo abrir automáticamente. Abra manualmente: " + mcpReportPath);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error generando reportes: " + e.getMessage());
            }
            
            // Mostrar recomendaciones adicionales
            System.out.println("\n\n" + "=".repeat(80));
            System.out.println("📋 REPORTES GENERADOS:");
            System.out.println("   • HTML Principal: mcp-analysis-report.html");
            System.out.println("   • JSON Data: mcp-analysis-report.json");
            System.out.println("   • HTML Respaldo: policy-analysis-report.html");
            System.out.println("📋 Para ver la guía completa de soluciones, ejecute PolicyFixRecommendations.main()");
            System.out.println("=".repeat(80));
            
        } else {
            System.out.println("⚠️  No se obtuvieron resultados del análisis");
        }
        
        System.out.println("\n┌────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                            ✅ ANÁLISIS COMPLETADO                            │");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────┘");
    }
}