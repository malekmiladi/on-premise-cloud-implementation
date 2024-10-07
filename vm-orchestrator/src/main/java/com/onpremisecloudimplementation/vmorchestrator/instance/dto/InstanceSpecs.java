package com.onpremisecloudimplementation.vmorchestrator.instance.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceSpecs {
    
    private UUID id;
    private String region;
    private Integer memorySizeGb;
    private Integer diskSizeGb;
    private Integer cpusCount;
    private InstanceImage image;

}
