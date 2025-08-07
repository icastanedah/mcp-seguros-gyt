package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PolicyChecker {
    
    public static String checkPolicies(String directoryPath) {
        try {
            // Simular la respuesta de check_policies basada en tu ejemplo
            return simulateCheckPolicies(directoryPath);
        } catch (Exception e) {
            return "Error al verificar políticas: " + e.getMessage();
        }
    }
    
    private static String simulateCheckPolicies(String directoryPath) {
        StringBuilder result = new StringBuilder();
        
        result.append("🔍 ANÁLISIS DE POLÍTICAS DE DESARROLLO\n");
        result.append("📁 ").append(directoryPath).append("\n");
        result.append("📊 Violaciones encontradas: 687\n\n");
        
        // Simular algunas violaciones basadas en tu ejemplo real
        String[] violations = {
            "? HARDCODED_STRING\n? CancellationDataSourceConfiguration.java:23\n? @EnableJpaRepositories(basePackages = \"gt.com.gyt.seguros.policy.cancellation.management.configuration.repository\",\n? Strings largos deben externalizarse",
            
            "? HARDCODED_STRING\n? CancellationDataSourceConfiguration.java:24\n? entityManagerFactoryRef = \"cancellationEntityManagerFactory\",\n? Strings largos deben externalizarse",
            
            "? METODO_CAMELCASE\n? CancellationDataSourceConfiguration.java:26\n? public class CancellationDataSourceConfiguration {\n? Nombres de métodos deben usar camelCase",
            
            "? JAVADOC_PUBLICO\n? CancellationDataSourceConfiguration.java:26\n? public class CancellationDataSourceConfiguration {\n? Clases públicas requieren JavaDoc",
            
            "? EXCEPTION_GENERICA\n? BatchDetailSvcImpl.java:161\n? } catch (Exception e) {\n? Capturar excepciones específicas, no Exception genérica",
            
            "? NUMERO_MAGICO\n? BatchDetailSvcImpl.java:335\n? if (!daysLateValidate) reasonCancellationList.add(\"La poliza tiene menos de 45 dias de atraso\");\n? Usar constantes nombradas en lugar de números mágicos",
            
            "? IMPORT_WILDCARD\n? PaystubData.java:4\n? import lombok.*;\n? Evitar imports con wildcard (*)",
            
            "? VARIABLE_CAMELCASE\n? BatchDetailSpecification.java:14\n? String DAYS_LATE = \"daysLate\";\n? Variables deben usar camelCase",
            
            "? METODO_PUBLICO_JAVADOC\n? BatchDetailSvcImpl.java:84\n? public CancellationBatchDetail getCancellationBatchDetailById(Long id) {\n? Métodos públicos requieren JavaDoc",
            
            "? CONSTANTE_UPPERCASE\n? AttachmentDTO.java:15\n? private static final long serialVersionUID = 1L;\n? Constantes deben usar UPPER_CASE"
        };
        
        // Agregar las violaciones al resultado
        for (String violation : violations) {
            result.append(violation).append("\n\n");
        }
        
        // Agregar resumen final
        result.append("📊 RESUMEN:\n");
        result.append("🔴 ALTO: 296\n");
        result.append("🟢 BAJO: 1\n");
        result.append("🟡 MEDIO: 390\n");
        
        return result.toString();
    }
    
    public static void main(String[] args) {
        String testPath = "C:\\Users\\icastaneda\\Documents\\policy-cancellation-management";
        String result = checkPolicies(testPath);
        System.out.println(result);
    }
}