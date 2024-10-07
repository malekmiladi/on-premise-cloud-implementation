package com.onpremisecloudimplementation.vmorchestrator.instance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceTaskError {
    private Integer type;
    private String message;
}
