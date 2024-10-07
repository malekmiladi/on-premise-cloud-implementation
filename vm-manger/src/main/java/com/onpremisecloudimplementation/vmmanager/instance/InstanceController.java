package com.onpremisecloudimplementation.vmmanager.instance;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.onpremisecloudimplementation.vmmanager.instance.dto.NewInstance;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/instances")
public class InstanceController {
    
    private final InstanceService instanceService;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> createNewInstance(@PathVariable UUID projectId, @RequestBody NewInstance newInstance) {
        UUID progressId = instanceService.createNewInstance(projectId, newInstance);
        Map<String, UUID> response = new HashMap<>();
        response.put("statusId", progressId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("{instanceId}")
    public ResponseEntity<HttpStatus> shutdownInstance(@PathVariable UUID projectId, @PathVariable UUID instanceId) {
        return new ResponseEntity<>(instanceService.shutdownInstance(projectId, instanceId));
    }
    
    @DeleteMapping("{instanceId}")
    public ResponseEntity<HttpStatus> deleteInstance(@PathVariable UUID projectId, @PathVariable UUID instanceId) {
        return new ResponseEntity<>(instanceService.deleteInstance(projectId, instanceId));
    }
    
}
