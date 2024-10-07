package com.onpremisecloudimplementation.vmmanager.instance.dto;

import java.sql.Date;
import java.util.UUID;

import com.onpremisecloudimplementation.vmmanager.image.dto.InstanceImage;

import lombok.Data;

@Data
public class InstanceData {
    private UUID id;
    private UUID userId;
    private Date createdAt;
    private String region;
    private Integer memorySizeGb;
    private Integer diskSizeGb;
    private Integer cpusCount;
    private String name;
    private String description;
    private String internalIp;
    private String externalIp;
    private String status;
    private InstanceImage image;
}
