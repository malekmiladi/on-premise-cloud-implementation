package com.onpremisecloudimplementation.vmmanager.instance.dto;

import lombok.Data;

@Data
public class OrchestratorError {
    private Integer type;
    private String message;
}
