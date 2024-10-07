package com.onpremisecloudimplementation.vmmanager.instance.dto;

import lombok.Data;

@Data
public class OrchestratorResponse {
    private Boolean ack;
    private OrchestratorError error;
    private InstanceMetadata metadata;
}
