package com.onpremisecloudimplementation.vmmanager.instance.dto;

import java.util.UUID;

import com.onpremisecloudimplementation.vmmanager.image.dto.InstanceImage;

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
