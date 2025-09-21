package com.docheck.flow.api;

import com.docheck.flow.api.dto.MonitoredComponentDTO;
import com.docheck.flow.api.dto.MonitoredComponentRequest;
import com.docheck.flow.api.dto.SystemHealthAcknowledgeRequest;
import com.docheck.flow.api.dto.SystemHealthConfigDTO;
import com.docheck.flow.api.dto.SystemHealthEventRequest;
import com.docheck.flow.application.service.SystemHealthEventService;
import com.docheck.flow.application.service.MonitoredComponentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/system-health")
public class SystemHealthController {

    private final SystemHealthEventService service;
    private final MonitoredComponentService componentService;

    public SystemHealthController(SystemHealthEventService service,
                                  MonitoredComponentService componentService) {
        this.service = service;
        this.componentService = componentService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> recordEvent(@RequestBody SystemHealthEventRequest request) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.recordEvent(request)
        ));
    }

    @GetMapping("/events")
    public ResponseEntity<?> listEvents(@RequestParam(value = "acknowledged", required = false) Boolean acknowledged,
                                        @RequestParam(value = "severity", required = false) String severity,
                                        @RequestParam(value = "since", required = false) Long sinceEpochMillis) {
        Optional<Instant> since = sinceEpochMillis != null ? Optional.of(Instant.ofEpochMilli(sinceEpochMillis)) : Optional.empty();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.list(Optional.ofNullable(acknowledged), Optional.ofNullable(severity), since)
        ));
    }

    @PatchMapping("/events/{eventId}/ack")
    public ResponseEntity<?> acknowledge(@PathVariable("eventId") String eventId,
                                         @RequestBody SystemHealthAcknowledgeRequest request) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.acknowledge(eventId, request.getReviewerId(), request.getReviewerName(), request.getNotes())
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.buildSummary()
        ));
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.getConfig()
        ));
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateConfig(@RequestBody SystemHealthConfigDTO request) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", service.updateConfig(request)
        ));
    }

    @GetMapping("/components")
    public ResponseEntity<?> listComponents() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", componentService.list()
        ));
    }

    @PostMapping("/components")
    public ResponseEntity<?> createComponent(@RequestBody MonitoredComponentRequest request) {
        MonitoredComponentDTO dto = componentService.create(request);
        return ResponseEntity.ok(Map.of("success", true, "data", dto));
    }

    @PutMapping("/components/{id}")
    public ResponseEntity<?> updateComponent(@PathVariable("id") String id,
                                             @RequestBody MonitoredComponentRequest request) {
        MonitoredComponentDTO dto = componentService.update(id, request);
        return ResponseEntity.ok(Map.of("success", true, "data", dto));
    }

    @DeleteMapping("/components/{id}")
    public ResponseEntity<?> deleteComponent(@PathVariable("id") String id) {
        componentService.delete(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
