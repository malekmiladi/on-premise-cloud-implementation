package com.onpremisecloudimplementation.vmcomputenode.instance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachineDeletionRequest {
    private String instanceId;
}
