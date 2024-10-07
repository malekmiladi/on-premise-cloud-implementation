package com.onpremisecloudimplementation.vmmanager.image.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceImage {
 
    private UUID id;
    private String downloadUrl;

}