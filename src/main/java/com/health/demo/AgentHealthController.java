package com.health.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/agents")
public class AgentHealthController {

    // agentId → last heartbeat
    private final ConcurrentMap<String, AgentHealthPayload> lastHealth = new ConcurrentHashMap<>();

    // ✅ Only updated when agent PUSHES data
    @PostMapping("/{agentId}/health")
    public ResponseEntity<?> updateHealth(
            @PathVariable String agentId,
            @RequestBody AgentHealthPayload payload) {

        payload.setAgentId(agentId);
        payload.setTimestamp(Instant.now());  // force server-side timestamp
        lastHealth.put(agentId, payload);     // UPDATE ONLY HERE

        return ResponseEntity.ok(Map.of(
                "status", "HEARTBEAT_RECEIVED",
                "agentId", agentId
        ));
    }

    // ✅ NO JSON UPDATE HERE — only health check
    @GetMapping("/{agentId}/health")
    public ResponseEntity<?> getHealth(@PathVariable String agentId) {

        AgentHealthPayload payload = lastHealth.get(agentId);

        if (payload == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "agentId", agentId,
                    "alive", false,
                    "reason", "No heartbeat ever received"
            ));
        }

        long seconds = Duration.between(
                payload.getTimestamp(),
                Instant.now()
        ).getSeconds();

        boolean alive = seconds <= 10;   //  DEAD AFTER 10 SECONDS

        return ResponseEntity.ok(Map.of(
                "agentId", agentId,
                "alive", alive,
                "lastSeenSecondsAgo", seconds,
                "lastHealth", payload   //  LAST DATA ONLY — NOT UPDATED
        ));
    }

    // View all agents
    @GetMapping("/health")
    public ResponseEntity<?> getAllAgents() {
        return ResponseEntity.ok(lastHealth);
    }
}
