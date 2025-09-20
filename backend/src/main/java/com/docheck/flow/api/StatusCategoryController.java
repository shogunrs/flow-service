package com.docheck.flow.api;

import com.docheck.flow.application.service.StatusCategoryService;
import com.docheck.flow.domain.model.StatusCategory;
import com.docheck.flow.domain.model.StatusItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/status-categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StatusCategoryController {

    private final StatusCategoryService service;

    @Autowired
    public StatusCategoryController(StatusCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StatusCategory>> getAllCategories(@RequestParam(required = false) String search) {
        List<StatusCategory> categories;
        if (search != null && !search.trim().isEmpty()) {
            categories = service.searchCategories(search);
        } else {
            categories = service.getAllCategories();
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusCategory> getCategoryById(@PathVariable String id) {
        return service.getCategoryById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StatusCategory> createCategory(@RequestBody CreateCategoryRequest request) {
        try {
            StatusCategory category = service.createCategory(
                    request.name,
                    request.description,
                    request.icon
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatusCategory> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request) {
        try {
            StatusCategory category = service.updateCategory(
                    id,
                    request.name,
                    request.description,
                    request.icon
            );
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        try {
            service.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{categoryId}/statuses")
    public ResponseEntity<StatusCategory> addStatus(
            @PathVariable String categoryId,
            @RequestBody CreateStatusRequest request) {
        try {
            StatusCategory category = service.addStatusToCategory(
                    categoryId,
                    request.name,
                    request.description,
                    request.color
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{categoryId}/statuses/{statusId}")
    public ResponseEntity<StatusCategory> updateStatus(
            @PathVariable String categoryId,
            @PathVariable String statusId,
            @RequestBody UpdateStatusRequest request) {
        try {
            StatusCategory category = service.updateStatus(
                    categoryId,
                    statusId,
                    request.name,
                    request.description,
                    request.color
            );
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{categoryId}/statuses/{statusId}")
    public ResponseEntity<StatusCategory> deleteStatus(
            @PathVariable String categoryId,
            @PathVariable String statusId) {
        try {
            StatusCategory category = service.removeStatusFromCategory(categoryId, statusId);
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{categoryId}/statuses")
    public ResponseEntity<List<StatusItem>> getCategoryStatuses(@PathVariable String categoryId) {
        List<StatusItem> statuses = service.getAllStatusesFromCategory(categoryId);
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<StatusItem> getStatusById(@PathVariable String statusId) {
        return service.findStatusById(statusId)
                .map(status -> ResponseEntity.ok(status))
                .orElse(ResponseEntity.notFound().build());
    }

    // DTOs
    public static class CreateCategoryRequest {
        public String name;
        public String description;
        public String icon;
    }

    public static class UpdateCategoryRequest {
        public String name;
        public String description;
        public String icon;
    }

    public static class CreateStatusRequest {
        public String name;
        public String description;
        public String color;
    }

    public static class UpdateStatusRequest {
        public String name;
        public String description;
        public String color;
    }
}