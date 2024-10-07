package com.onpremisecloudimplementation.vmorchestrator.computenode;

import com.onpremisecloudimplementation.grpcservices.ComputeNodeRegisterationData;
import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;
import com.onpremisecloudimplementation.vmorchestrator.vmorchestratorgrpcclient.HeartbeatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComputeNodeService {

    private final ComputeNodeRepository computeNodeRepository;
    private final HeartbeatClient managerGrpcClient;

    public UUID registerComputeNode(ComputeNodeRegisterationData newComputeNode) {
        if (!newComputeNode.getNodeId().isEmpty()) {
            ComputeNode existingComputeNode = computeNodeRepository
                    .findByNodeId(UUID.fromString(newComputeNode.getNodeId())).orElse(null);
            if (existingComputeNode != null) {
                return null;
            }
        }
        ComputeNode computeNode = new ComputeNode();
        computeNode.setDefaultMemoryGb(newComputeNode.getDefaultMemoryGb());
        computeNode.setAvailableMemoryGb(newComputeNode.getDefaultMemoryGb());
        computeNode.setDefaultStorageGb(newComputeNode.getDefaultStorageGb());
        computeNode.setAvailableStorageGb(newComputeNode.getDefaultStorageGb());
        computeNode.setDefaultCpus(newComputeNode.getDefaultCpus());
        computeNode.setAvailableCpus(newComputeNode.getDefaultCpus());
        computeNode.setGrpcUrlString(newComputeNode.getGrpcUrlString());
        computeNode.setRegion(newComputeNode.getRegion());
        computeNode.setState("active");
        computeNode.setInstances(new ArrayList<>());
        computeNode = computeNodeRepository.save(computeNode);
        return computeNode.getNodeId();
    }

    @Scheduled(fixedRate = 5000)
    public void updateComputeNodesState() {
        // TODO: make nodes send the request rather than making the orchestrator do it
        List<ComputeNode> computeNodes = computeNodeRepository.findAll();
        for (ComputeNode computeNode : computeNodes) {
            Boolean isAlive = managerGrpcClient.sendHeartbeatRequest(computeNode);
            if (!isAlive) {
                if (!computeNode.getState().equals("inactive")) {
                    computeNode.setState("inactive");
                }
            } else {
                if (!computeNode.getState().equals("active")) {
                    computeNode.setState("active");
                }
            }
            computeNodeRepository.save(computeNode);
        }
    }

}
