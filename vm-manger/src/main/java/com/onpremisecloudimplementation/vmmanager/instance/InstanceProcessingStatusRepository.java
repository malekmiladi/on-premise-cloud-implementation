package com.onpremisecloudimplementation.vmmanager.instance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmmanager.instance.dao.InstanceProcessingStatus;

public interface InstanceProcessingStatusRepository extends JpaRepository<InstanceProcessingStatus, UUID> {
    
}
