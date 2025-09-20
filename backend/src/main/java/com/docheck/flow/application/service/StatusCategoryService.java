package com.docheck.flow.application.service;

import com.docheck.flow.application.port.StatusCategoryRepository;
import com.docheck.flow.domain.model.StatusCategory;
import com.docheck.flow.domain.model.StatusItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StatusCategoryService {

    private final StatusCategoryRepository repository;

    @Autowired
    public StatusCategoryService(StatusCategoryRepository repository) {
        this.repository = repository;
    }

    public List<StatusCategory> getAllCategories() {
        return repository.findAll();
    }

    public Optional<StatusCategory> getCategoryById(String id) {
        return repository.findById(id);
    }

    public List<StatusCategory> searchCategories(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCategories();
        }
        return repository.findByNameContaining(query.trim());
    }

    public StatusCategory createCategory(String name, String description, String icon) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }

        StatusCategory category = new StatusCategory();
        category.setId(UUID.randomUUID().toString());
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setStatuses(new ArrayList<>());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return repository.save(category);
    }

    public StatusCategory updateCategory(String id, String name, String description, String icon) {
        StatusCategory category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if name is taken by another category
        Optional<StatusCategory> existingCategory = repository.findByName(name);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }

        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setUpdatedAt(LocalDateTime.now());

        return repository.save(category);
    }

    public void deleteCategory(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Category not found");
        }
        repository.deleteById(id);
    }

    public StatusCategory addStatusToCategory(String categoryId, String statusName, String description, String color) {
        StatusCategory category = repository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if status name already exists in this category
        boolean statusExists = category.getStatuses().stream()
                .anyMatch(status -> status.getName().equalsIgnoreCase(statusName));

        if (statusExists) {
            throw new IllegalArgumentException("Status with name '" + statusName + "' already exists in this category");
        }

        StatusItem newStatus = new StatusItem();
        newStatus.setId(UUID.randomUUID().toString());
        newStatus.setName(statusName);
        newStatus.setDescription(description);
        newStatus.setColor(color);
        newStatus.setCategoryId(categoryId);
        newStatus.setCreatedAt(LocalDateTime.now());
        newStatus.setUpdatedAt(LocalDateTime.now());

        if (category.getStatuses() == null) {
            category.setStatuses(new ArrayList<>());
        }

        category.getStatuses().add(newStatus);
        category.setUpdatedAt(LocalDateTime.now());

        return repository.save(category);
    }

    public StatusCategory updateStatus(String categoryId, String statusId, String statusName, String description, String color) {
        StatusCategory category = repository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        StatusItem status = category.getStatuses().stream()
                .filter(s -> s.getId().equals(statusId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));

        // Check if status name already exists in this category (excluding current status)
        boolean statusExists = category.getStatuses().stream()
                .anyMatch(s -> !s.getId().equals(statusId) && s.getName().equalsIgnoreCase(statusName));

        if (statusExists) {
            throw new IllegalArgumentException("Status with name '" + statusName + "' already exists in this category");
        }

        status.setName(statusName);
        status.setDescription(description);
        status.setColor(color);
        status.setUpdatedAt(LocalDateTime.now());

        category.setUpdatedAt(LocalDateTime.now());

        return repository.save(category);
    }

    public StatusCategory removeStatusFromCategory(String categoryId, String statusId) {
        StatusCategory category = repository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        boolean removed = category.getStatuses().removeIf(status -> status.getId().equals(statusId));

        if (!removed) {
            throw new IllegalArgumentException("Status not found in category");
        }

        category.setUpdatedAt(LocalDateTime.now());

        return repository.save(category);
    }

    public Optional<StatusItem> findStatusById(String statusId) {
        Optional<StatusCategory> categoryOpt = repository.findByStatusId(statusId);
        if (categoryOpt.isPresent()) {
            StatusCategory category = categoryOpt.get();
            return category.getStatuses().stream()
                    .filter(status -> status.getId().equals(statusId))
                    .findFirst();
        }
        return Optional.empty();
    }

    public List<StatusItem> getAllStatusesFromCategory(String categoryId) {
        return repository.findById(categoryId)
                .map(StatusCategory::getStatuses)
                .orElse(new ArrayList<>());
    }
}