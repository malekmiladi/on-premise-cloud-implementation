package com.onpremisecloudimplementation.vmorchestrator.instance;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.onpremisecloudimplementation.grpcservices.InstanceImageData;
import com.onpremisecloudimplementation.grpcservices.InstanceShutdownOrDeleteTarget;
import com.onpremisecloudimplementation.grpcservices.InstanceSpecifications;
import com.onpremisecloudimplementation.grpcservices.ComputeNodeResponse;
import com.onpremisecloudimplementation.vmorchestrator.computenode.ComputeNodeRepository;
import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;
import com.onpremisecloudimplementation.vmorchestrator.instance.dao.Instance;
import com.onpremisecloudimplementation.vmorchestrator.instance.dto.InstanceSpecs;
import com.onpremisecloudimplementation.vmorchestrator.instance.dto.NewInstanceResponse;
import com.onpremisecloudimplementation.vmorchestrator.instance.dto.InstanceShutdownOrDeleteRequest;
import com.onpremisecloudimplementation.vmorchestrator.instance.dto.InstanceTaskError;
import com.onpremisecloudimplementation.vmorchestrator.instance.dto.NewInstanceMetadata;
import com.onpremisecloudimplementation.vmorchestrator.vmorchestratorgrpcclient.InstanceActionsServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final ComputeNodeRepository computeNodeRepository;
    private final InstanceActionsServiceClient instanceActionsServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final ModelMapper modelMapper = new ModelMapper();

    @KafkaListener(groupId = "orchestrator-instance-create-requests", topics = "orchestrator-instance-create-requests")
    public void processInstanceSpecifications(InstanceSpecs newInstanceRequest) {
        NewInstanceResponse response;
        InstanceTaskError error = new InstanceTaskError(0, "");
        NewInstanceMetadata metadata = new NewInstanceMetadata(null, "", "", "");
        boolean ack = false;

        // TODO: log to file
        log.info("**** New instance request: cpus={} size={} memory={} {}", newInstanceRequest.getCpusCount(), newInstanceRequest.getDiskSizeGb(), newInstanceRequest.getMemorySizeGb(), newInstanceRequest.getRegion());

        ComputeNode node = findAdequateServer(newInstanceRequest);

        if (node == null) {
            // TODO: Add logic for handling errors (enumerate and name errors instead of
            // using plain integers)
            error = new InstanceTaskError(1, "Couldn't find a node for requested VM.");
        } else {
            UUID newInstanceId = allocateResources(node, newInstanceRequest);
            ComputeNodeResponse grpcResponse = sendInstanceSpecs(newInstanceId, newInstanceRequest, node);
            if (!grpcResponse.getAck()) {
                error = new InstanceTaskError(2, grpcResponse.getError().getMessage());

            } else {
                ack = true;
                metadata = new NewInstanceMetadata(newInstanceRequest.getId(),
                        grpcResponse.getMetadata().getInternalIp(), grpcResponse.getMetadata().getExternalIp(),
                        grpcResponse.getMetadata().getState());
            }
        }
        response = new NewInstanceResponse(ack, error, metadata);
        kafkaTemplate.send("orchestrator-instance-create-responses", response);
    }

    @KafkaListener(groupId = "orchestrator-instance-shutdown-requests", topics = "orchestrator-instance-shutdown-requests")
    public void shutdownInstance(InstanceShutdownOrDeleteRequest shutdownRequest) {
        log.info("**** Shutdown request for instance: instance.id={}", shutdownRequest.getInstanceId().toString());

        Instance instance = instanceRepository.findByName(shutdownRequest.getInstanceId());
        ComputeNodeResponse nodeResponse = sendInstanceShutdownRequest(instance.getInstanceId());
        boolean ack = false;
        InstanceTaskError error = new InstanceTaskError(0, "");
        NewInstanceMetadata metadata = new NewInstanceMetadata(null, "", "", "");
        NewInstanceResponse response;

        if (nodeResponse.getAck()) {
            metadata = new NewInstanceMetadata(shutdownRequest.getInstanceId(), "", "", "shutdown");
            ack = true;
        } else {
            error = new InstanceTaskError(nodeResponse.getError().getType(), nodeResponse.getError().getMessage());
        }
        response = new NewInstanceResponse(ack, error, metadata);
        kafkaTemplate.send("orchestrator-instance-shutdown-responses", response);
    }

    @KafkaListener(groupId = "orchestrator-instance-delete-requests", topics = "orchestrator-instance-delete-requests")
    public void deleteInstance(InstanceShutdownOrDeleteRequest deleteRequest) {
        log.info("**** Delete request for instance: instance.id={}", deleteRequest.getInstanceId());
        Instance instance = instanceRepository.findByName(deleteRequest.getInstanceId());
        ComputeNodeResponse nodeResponse = sendInstanceDeleteRequest(instance.getInstanceId());
        boolean ack = false;
        InstanceTaskError error = new InstanceTaskError(0, "");
        NewInstanceMetadata metadata = new NewInstanceMetadata(null, "", "", "");
        NewInstanceResponse response;

        if (nodeResponse.getAck()) {
            ack = true;
            metadata = new NewInstanceMetadata(deleteRequest.getInstanceId(), "", "", "deleted");
            instanceRepository.delete(instance);
        } else {
            error = new InstanceTaskError(nodeResponse.getError().getType(), nodeResponse.getError().getMessage());
        }

        response = new NewInstanceResponse(ack, error, metadata);
        kafkaTemplate.send("orchestrator-instance-delete-responses", response);
    }

    public ComputeNode findAdequateServer(InstanceSpecs instanceSpecs) {
        ComputeNode candidateServer;
        candidateServer = computeNodeRepository
                .findCandidateInstance(
                        instanceSpecs.getMemorySizeGb(), instanceSpecs.getDiskSizeGb(), instanceSpecs.getCpusCount(),
                        instanceSpecs.getRegion(), "active")
                .orElse(null);

        return candidateServer;
    }

    public UUID allocateResources(ComputeNode targetComputeNode, InstanceSpecs instanceSpecs) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Instance newInstance = modelMapper.map(instanceSpecs, Instance.class);
        newInstance.setInstanceId(null);
        newInstance.setName(instanceSpecs.getId());
        newInstance.setComputeNode(targetComputeNode);
        newInstance = instanceRepository.save(newInstance);

        targetComputeNode.setAvailableCpus(targetComputeNode.getAvailableCpus() - instanceSpecs.getCpusCount());
        targetComputeNode
                .setAvailableMemoryGb(targetComputeNode.getAvailableMemoryGb() - instanceSpecs.getMemorySizeGb());
        targetComputeNode
                .setAvailableStorageGb(targetComputeNode.getAvailableStorageGb() - instanceSpecs.getDiskSizeGb());
        computeNodeRepository.save(targetComputeNode);

        return newInstance.getInstanceId();
    }

    public ComputeNodeResponse sendInstanceSpecs(UUID newInstanceId, InstanceSpecs instance,
            ComputeNode targetComputeNode) {
        InstanceImageData image = InstanceImageData.newBuilder()
                .setDownloadUrl(instance.getImage().getDownloadUrl())
                .build();

        InstanceSpecifications instanceSpecs = InstanceSpecifications.newBuilder()
                .setInstanceId(newInstanceId.toString())
                .setInstanceName(instance.getId().toString())
                .setCpusCount(instance.getCpusCount())
                .setMemorySizeGb(instance.getMemorySizeGb())
                .setImage(image)
                .setDiskSizeGb(instance.getDiskSizeGb())
                .build();

        return instanceActionsServiceClient.sendCreateInstanceRequest(targetComputeNode, instanceSpecs);
    }

    public ComputeNodeResponse sendInstanceShutdownRequest(UUID instanceId) {
        Instance instance = instanceRepository.findByInstanceId(instanceId);
        ComputeNode targetNode = instance.getComputeNode();
        InstanceShutdownOrDeleteTarget shutdownRequest = InstanceShutdownOrDeleteTarget.newBuilder()
                .setInstanceId(instance.getInstanceId().toString())
                .build();

        return instanceActionsServiceClient.sendInstanceShutdownRequest(targetNode, shutdownRequest);
    }

    public ComputeNodeResponse sendInstanceDeleteRequest(UUID instanceId) {
        Instance instance = instanceRepository.findByInstanceId(instanceId);
        ComputeNode targetNode = instance.getComputeNode();
        InstanceShutdownOrDeleteTarget deleteRequest = InstanceShutdownOrDeleteTarget.newBuilder()
                .setInstanceId(instance.getInstanceId().toString())
                .build();

        return instanceActionsServiceClient.sendInstanceDeleteRequest(targetNode, deleteRequest);
    }

}
