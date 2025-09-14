package com.docheck.flow.infrastructure.config;

import com.docheck.flow.application.service.StatusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final StatusService statusService;

    @Value("${app.statuses:Pendente,Em análise,Aprovado,Reprovado,Arquivado}")
    private String defaultStatuses;

    public DataInitializer(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Initialize default statuses if none exist
        if (statusService.findAll().isEmpty()) {
            initializeDefaultStatuses();
        }
    }

    private void initializeDefaultStatuses() {
        List<String> statusNames = Arrays.stream(defaultStatuses.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // Default colors for each status
        String[] colors = {"#f59e0b", "#3b82f6", "#10b981", "#ef4444", "#6b7280"};

        for (int i = 0; i < statusNames.size(); i++) {
            String name = statusNames.get(i);
            String color = i < colors.length ? colors[i] : "#3b82f6"; // Default to blue

            try {
                statusService.create(name, color);
                System.out.println("Created default status: " + name + " with color: " + color);
            } catch (Exception e) {
                System.err.println("Failed to create status: " + name + " - " + e.getMessage());
            }
        }
    }
}