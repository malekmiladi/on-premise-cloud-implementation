package com.onpremisecloudimplementation.vmmanager.instance.dao;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "vm_instance_processing_status")
@Setter
@Getter
@NoArgsConstructor
public class InstanceProcessingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String status;
    
    @OneToOne
    @JoinColumn(name = "instance_id")
    private Instance instance;

}
