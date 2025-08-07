package org.example.mcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Clase de configuración para el MCP Security Analyzer
 * Maneja variables de entorno, configuraciones por defecto y validaciones
 */
public class Config {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "mcp-config.properties";
    
    // Configuraciones por defecto
    private static final int DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int DEFAULT_MAX_LINE_LENGTH = 10000; // 10KB
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int DEFAULT_MAX_DEPTH = 10;
    
    static {
        loadConfiguration();
    }
    
    /**
     * Carga la configuración desde archivo y variables de entorno
     */
    private static void loadConfiguration() {
        // Cargar desde archivo de configuración si existe
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (InputStream input = new FileInputStream(configPath.toFile())) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Warning: No se pudo cargar el archivo de configuración: " + e.getMessage());
            }
        }
        
        // Cargar desde variables de entorno (tienen prioridad)
        loadFromEnvironment();
    }
    
    /**
     * Carga configuración desde variables de entorno
     */
    private static void loadFromEnvironment() {
        // Tamaño máximo de archivo
        String maxFileSize = System.getenv("MCP_MAX_FILE_SIZE");
        if (maxFileSize != null) {
            try {
                properties.setProperty("max.file.size", maxFileSize);
            } catch (NumberFormatException e) {
                System.err.println("Warning: MCP_MAX_FILE_SIZE inválido: " + maxFileSize);
            }
        }
        
        // Longitud máxima de línea
        String maxLineLength = System.getenv("MCP_MAX_LINE_LENGTH");
        if (maxLineLength != null) {
            try {
                properties.setProperty("max.line.length", maxLineLength);
            } catch (NumberFormatException e) {
                System.err.println("Warning: MCP_MAX_LINE_LENGTH inválido: " + maxLineLength);
            }
        }
        
        // Encoding
        String encoding = System.getenv("MCP_ENCODING");
        if (encoding != null) {
            properties.setProperty("file.encoding", encoding);
        }
        
        // Profundidad máxima
        String maxDepth = System.getenv("MCP_MAX_DEPTH");
        if (maxDepth != null) {
            try {
                properties.setProperty("max.depth", maxDepth);
            } catch (NumberFormatException e) {
                System.err.println("Warning: MCP_MAX_DEPTH inválido: " + maxDepth);
            }
        }
    }
    
    /**
     * Obtiene el tamaño máximo de archivo permitido
     */
    public static int getMaxFileSize() {
        String value = properties.getProperty("max.file.size", String.valueOf(DEFAULT_MAX_FILE_SIZE));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_FILE_SIZE;
        }
    }
    
    /**
     * Obtiene la longitud máxima de línea permitida
     */
    public static int getMaxLineLength() {
        String value = properties.getProperty("max.line.length", String.valueOf(DEFAULT_MAX_LINE_LENGTH));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_LINE_LENGTH;
        }
    }
    
    /**
     * Obtiene el encoding para archivos
     */
    public static String getFileEncoding() {
        return properties.getProperty("file.encoding", DEFAULT_ENCODING);
    }
    
    /**
     * Obtiene la profundidad máxima para recorrido de directorios
     */
    public static int getMaxDepth() {
        String value = properties.getProperty("max.depth", String.valueOf(DEFAULT_MAX_DEPTH));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_DEPTH;
        }
    }
    
    /**
     * Valida si un path es seguro para procesar
     */
    public static boolean isPathSafe(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path normalizedPath = Paths.get(path).normalize();
            
            // Verificar que no contenga secuencias peligrosas
            String pathStr = normalizedPath.toString();
            if (pathStr.contains("..") || pathStr.contains("~")) {
                return false;
            }
            
            // Para paths relativos, verificar si existe desde el directorio actual
            Path fullPath;
            if (normalizedPath.isAbsolute()) {
                fullPath = normalizedPath;
            } else {
                // Si el path comienza con "Documents/", intentar resolver desde el home del usuario
                if (path.startsWith("Documents/") || path.startsWith("Documents\\")) {
                    String homeDir = System.getProperty("user.home");
                    fullPath = Paths.get(homeDir).resolve(path).normalize();
                } else {
                    fullPath = Paths.get(getWorkingDirectory()).resolve(normalizedPath).normalize();
                }
            }
            
            // Verificar que existe y es legible
            return Files.exists(fullPath) && Files.isReadable(fullPath);
            
        } catch (Exception e) {
            System.err.println("Error validando path '" + path + "': " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene el directorio de trabajo actual
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Obtiene el separador de archivos del sistema
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    /**
     * Obtiene el separador de líneas del sistema
     */
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    /**
     * Verifica si el sistema operativo es Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    /**
     * Verifica si el sistema operativo es macOS
     */
    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * Verifica si el sistema operativo es Linux
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
    
    /**
     * Obtiene información del sistema
     */
    public static String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        info.append("Java: ").append(System.getProperty("java.version")).append("\n");
        info.append("Architecture: ").append(System.getProperty("os.arch")).append("\n");
        info.append("Working Directory: ").append(getWorkingDirectory()).append("\n");
        info.append("File Separator: ").append(getFileSeparator()).append("\n");
        info.append("Max File Size: ").append(getMaxFileSize()).append(" bytes\n");
        info.append("Max Line Length: ").append(getMaxLineLength()).append(" characters\n");
        info.append("Max Depth: ").append(getMaxDepth()).append(" levels\n");
        return info.toString();
    }
} 