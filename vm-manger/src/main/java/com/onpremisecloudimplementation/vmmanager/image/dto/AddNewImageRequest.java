package com.onpremisecloudimplementation.vmmanager.image.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddNewImageRequest {
    
    private UUID id;
    private String name;
    private Integer sizeGb;
    private Integer minDiskGb;
    private Integer minRAMGb;
    private String location;
    private String state;
    private Boolean isPublic;

}
