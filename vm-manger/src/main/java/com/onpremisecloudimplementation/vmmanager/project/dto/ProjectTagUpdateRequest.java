package com.onpremisecloudimplementation.vmmanager.project.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ProjectTagUpdateRequest {
    private UUID id;
    private String value = "";
}
