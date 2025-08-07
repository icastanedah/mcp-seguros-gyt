package org.example.mcp;

import java.util.*;

public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public static class Task {
        private final int id;
        private final String description;
        private boolean completed;

        public Task(int id, String description) {
            this.id = id;
            this.description = description;
            this.completed = false;
        }

        public int getId() { return id; }
        public String getDescription() { return description; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        @Override
        public String toString() {
            return String.format("[%d] %s %s", id, completed ? "✓" : "○", description);
        }
    }

    public String addTask(String description) {
        Task task = new Task(nextId++, description);
        tasks.add(task);
        return "Tarea agregada: " + task;
    }

    public String listTasks() {
        if (tasks.isEmpty()) {
            return "No hay tareas pendientes.";
        }
        
        StringBuilder sb = new StringBuilder("📋 Lista de tareas:\n");
        for (Task task : tasks) {
            sb.append(task).append("\n");
        }
        return sb.toString().trim();
    }

    public String completeTask(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.setCompleted(true);
                return "Tarea completada: " + task;
            }
        }
        return "Tarea no encontrada con ID: " + id;
    }

    public String deleteTask(int id) {
        tasks.removeIf(task -> {
            if (task.getId() == id) {
                return true;
            }
            return false;
        });
        return "Tarea eliminada con ID: " + id;
    }

    public String getStats() {
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long pending = tasks.size() - completed;
        return String.format("📊 Estadísticas: %d completadas, %d pendientes, %d total", 
                           completed, pending, tasks.size());
    }
}