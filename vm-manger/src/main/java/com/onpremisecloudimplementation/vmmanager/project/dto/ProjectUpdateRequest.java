package com.onpremisecloudimplementation.vmmanager.project.dto;

import lombok.Data;

@Data
public class ProjectUpdateRequest {
    private String name = "";
    private String description = "";
}
