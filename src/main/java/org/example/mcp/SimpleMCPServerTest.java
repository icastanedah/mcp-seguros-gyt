package org.example.mcp;

import java.util.*;

public class SimpleMCPServerTest {
    public static void main(String[] args) {
        System.out.println("Probando compilación básica...");
        
        // Probar instanciación de clases básicas
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("test", "value");
        
        System.out.println("✅ Compilación básica exitosa");
        System.out.println("Map test: " + testMap.get("test"));
    }
}