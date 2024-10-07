package com.onpremisecloudimplementation.vmmanager.instance.dto;

import lombok.Data;

@Data
public class NewInstance {
    private InstanceData instanceData;
    private String instanceConfig;
}
