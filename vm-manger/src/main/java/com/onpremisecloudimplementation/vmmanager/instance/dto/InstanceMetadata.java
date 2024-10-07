package com.onpremisecloudimplementation.vmmanager.instance.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class InstanceMetadata {
    private UUID id;
    private String internalIp;
    private String externalIp;
    private String state;
}
