package com.onpremisecloudimplementation.vmorchestrator.instance.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceImage {
    
    private UUID imageId;
    private String downloadUrl;

}
