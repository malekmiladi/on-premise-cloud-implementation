package com.onpremisecloudimplementation.vmorchestrator.instance.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewInstanceMetadata {
    private UUID instanceId;
    private String internalIp;
    private String externalIp;
    private String state;
}
