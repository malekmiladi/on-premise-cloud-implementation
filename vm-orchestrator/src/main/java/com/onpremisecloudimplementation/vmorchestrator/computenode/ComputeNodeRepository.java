package com.onpremisecloudimplementation.vmorchestrator.computenode;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ComputeNodeRepository extends JpaRepository<ComputeNode, UUID> {

    @Query(
            nativeQuery = true,
            value = "SELECT" +
                    "   node_id," +
                    "   default_memory_gb," +
                    "   default_storage_gb," +
                    "   default_cpus," +
                    "   available_memory_gb," +
                    "   available_storage_gb," +
                    "   available_cpus," +
                    "   grpc_url_string," +
                    "   region," +
                    "   state " +
                    "FROM compute_node " +
                    "WHERE" +
                    "   available_memory_gb >= :memory" +
                    "   AND available_storage_gb >= :storage" +
                    "   AND available_cpus >= :vcpus" +
                    "   AND region = :region" +
                    "   AND state = :state " +
                    "LIMIT 1"
    )
    Optional<ComputeNode> findCandidateInstance(
            @Param("memory") Integer memory,
            @Param("storage") Integer storage,
            @Param("vcpus") Integer vcpus,
            @Param("region") String region,
            @Param("state") String state
    );

    Optional<ComputeNode> findByNodeId(UUID nodeId);
    
    
}

