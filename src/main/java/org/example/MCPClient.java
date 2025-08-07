package org.example;

import java.io.*;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MCPClient {
    public static void main(String[] args) {
        try {
            // Obtener el directorio actual del proyecto
            String currentDir = System.getProperty("user.dir");
            Path projectPath = Paths.get(currentDir);
            
            // Construir el classpath de forma segura
            String classpath = projectPath.resolve("build").resolve("libs").resolve("*").toString();
            
            // Iniciar el servidor MCP como proceso con validación
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", classpath, "org.example.mcp.MCPServer");
            pb.directory(projectPath.toFile());
            
            // Configurar variables de entorno seguras
            Map<String, String> env = pb.environment();
            env.put("JAVA_OPTS", "-Xmx512m");
            
            Process serverProcess = pb.start();
            
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
            
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("🚀 Servidor MCP iniciado!");
            System.out.println("Comandos disponibles:");
            System.out.println("1 - Initialize");
            System.out.println("2 - List tools");
            System.out.println("3 - Scan repository");
            System.out.println("q - Quit");
            
            while (true) {
                System.out.print("\n> ");
                String input = scanner.nextLine();
                
                if (input == null || input.trim().isEmpty()) {
                    continue;
                }
                
                String command = "";
                switch (input.trim()) {
                    case "1":
                        command = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}";
                        break;
                    case "2":
                        command = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
                        break;
                    case "3":
                        // Usar el directorio actual como path por defecto
                        String repoPath = currentDir.replace("\\", "\\\\");
                        command = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"scan_repo\",\"arguments\":{\"repo_path\":\"" + repoPath + "\"}}}";
                        break;
                    case "q":
                        serverProcess.destroy();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Comando no válido");
                        continue;
                }
                
                writer.write(command);
                writer.newLine();
                writer.flush();
                
                String response = reader.readLine();
                if (response != null) {
                    System.out.println("📨 Respuesta: " + response);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}