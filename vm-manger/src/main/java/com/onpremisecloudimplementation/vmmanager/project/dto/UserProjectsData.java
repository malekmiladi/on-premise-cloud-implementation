package com.onpremisecloudimplementation.vmmanager.project.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.onpremisecloudimplementation.vmmanager.instance.dto.InstanceData;

import lombok.Data;

@Data
public class UserProjectsData {
    private UUID id;
    private UUID userId;
    private String name = "";
    private String description = "";
    private List<ProjectTagData> tags = new ArrayList<ProjectTagData>();
    private List<InstanceData> instances = new ArrayList<InstanceData>();
}
