package org.example;

import java.io.*;
import java.util.Scanner;

public class MCPClient {
    public static void main(String[] args) {
        try {
            // Iniciar el servidor MCP como proceso
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "build/libs/*", "org.example.mcp.MCPServer");
            pb.directory(new File("c:/Users/icastaneda/Documents/hackaton/mcp-graddle"));
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
                
                String command = "";
                switch (input) {
                    case "1":
                        command = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}";
                        break;
                    case "2":
                        command = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
                        break;
                    case "3":
                        command = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"scan_repo\",\"arguments\":{\"repo_path\":\"C:\\\\Users\\\\icastaneda\\\\Documents\\\\walli-new-team\\\\policy-management\"}}}";
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
                System.out.println("📨 Respuesta: " + response);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}