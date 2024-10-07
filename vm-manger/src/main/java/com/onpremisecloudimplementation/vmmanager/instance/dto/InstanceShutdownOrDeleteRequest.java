package com.onpremisecloudimplementation.vmmanager.instance.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class InstanceShutdownOrDeleteRequest {
    private UUID id;
}
