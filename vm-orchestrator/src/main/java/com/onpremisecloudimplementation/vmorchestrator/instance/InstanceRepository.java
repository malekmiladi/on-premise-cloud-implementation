package com.onpremisecloudimplementation.vmorchestrator.instance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmorchestrator.instance.dao.Instance;



public interface InstanceRepository extends JpaRepository<Instance, UUID> {
    Instance findByInstanceId(UUID instanceId);
    Instance findByName(UUID instanceId);
}