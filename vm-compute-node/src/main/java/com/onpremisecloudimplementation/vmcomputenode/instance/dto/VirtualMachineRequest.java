package com.onpremisecloudimplementation.vmcomputenode.instance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachineRequest {
    private String instanceId;
    private String operatingSystem;
    private int vcpus;
    private int vram;
    private int vdisk;
}
