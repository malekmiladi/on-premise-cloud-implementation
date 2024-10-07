package com.onpremisecloudimplementation.vmmanager.instance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.onpremisecloudimplementation.vmmanager.instance.dao.Instance;

public interface InstanceRepository extends JpaRepository<Instance, UUID> {
    Instance findByProjectIdAndId(UUID projectId, UUID instanceId);
}
