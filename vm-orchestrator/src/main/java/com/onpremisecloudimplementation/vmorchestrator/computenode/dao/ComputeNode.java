package com.onpremisecloudimplementation.vmorchestrator.computenode.dao;

import java.util.List;
import java.util.UUID;

import com.onpremisecloudimplementation.vmorchestrator.instance.dao.Instance;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "compute_node")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComputeNode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID nodeId;

    private Integer defaultMemoryGb;

    private Integer defaultStorageGb;

    private Integer defaultCpus;

    private Integer availableMemoryGb;

    private Integer availableStorageGb;

    private Integer availableCpus;

    private String grpcUrlString;

    private String region;

    private String state;

    @OneToMany(mappedBy = "computeNode")
    private List<Instance> instances;

}
