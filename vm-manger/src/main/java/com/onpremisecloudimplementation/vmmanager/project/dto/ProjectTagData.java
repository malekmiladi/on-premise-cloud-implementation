package com.onpremisecloudimplementation.vmmanager.project.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ProjectTagData {
    private UUID id;
    private String value = "";
}
