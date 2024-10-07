package com.onpremisecloudimplementation.vmmanager.project.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequest {
    
    private UUID userId;
    private String name = "";
    private String description = "";
    private List<ProjectTagCreateRequest> tags = new ArrayList<ProjectTagCreateRequest>();

}
