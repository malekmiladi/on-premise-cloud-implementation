package com.onpremisecloudimplementation.vmorchestrator.instance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewInstanceResponse {

    private Boolean ack;
    private InstanceTaskError error;
    private NewInstanceMetadata metadata;

}
