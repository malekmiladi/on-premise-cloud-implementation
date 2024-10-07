package com.onpremisecloudimplementation.vmmanager.instance;

import com.onpremisecloudimplementation.vmmanager.instance.dao.Instance;
import com.onpremisecloudimplementation.vmmanager.instance.dao.InstanceProcessingStatus;
import com.onpremisecloudimplementation.vmmanager.instance.dto.*;
import com.onpremisecloudimplementation.vmmanager.project.ProjectRepository;
import com.onpremisecloudimplementation.vmmanager.project.dao.Project;
import com.onpremisecloudimplementation.vmmanager.utils.GenericModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final InstanceProcessingStatusRepository processingStatusRepository;
    private final ProjectRepository projectRepository;
    private final GenericModelMapper genericModelMapper = new GenericModelMapper();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UUID createNewInstance(UUID projectId, NewInstance newInstance) {
        InstanceData newInstanceData = newInstance.getInstanceData();
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = null;
        if (optionalProject.isPresent()) {
            project = optionalProject.get();
        }
        Instance instance = genericModelMapper.map(newInstanceData, Instance.class);
        instance.setProject(project);
        instance = instanceRepository.save(instance);
        sendInstanceConfig(instance.getId(), newInstance.getInstanceConfig());
        
        InstanceProcessingStatus instanceProcessingStatus = new InstanceProcessingStatus();
        instanceProcessingStatus.setInstance(instance);
        instanceProcessingStatus.setStatus("creating");
        processingStatusRepository.save(instanceProcessingStatus);

        InstanceSpecs orchestratorRequest = genericModelMapper.map(instance, InstanceSpecs.class);
        orchestratorRequest.setId(instance.getId());

        kafkaTemplate.send("orchestrator-instance-create-requests", orchestratorRequest);
        return instanceProcessingStatus.getId();
    }

    @KafkaListener(id = "orchestrator-instance-create-responses", topics = "orchestrator-instance-create-responses")
    public void updateInstanceMetadata(OrchestratorResponse response) {
        log.info("**** Instance create response: {}", response.toString());
        if (response.getAck()) {
            InstanceMetadata metadata = response.getMetadata();
            Optional<Instance> optionalInstance = instanceRepository.findById(metadata.getId());
            if (optionalInstance.isPresent()) {
                Instance instance = optionalInstance.get();
                InstanceProcessingStatus processingStatus = instance.getStatus();
                processingStatus.setStatus("created");
                instance.setExternalIp(metadata.getExternalIp());
                instance.setInternalIp(metadata.getInternalIp());
                instance.setState(metadata.getState());
                instanceRepository.save(instance);
                processingStatusRepository.save(processingStatus);
            }
        } else {
            log.info(response.getError().getMessage());
            InstanceShutdownOrDeleteRequest cloudDatasourceDelReq = new InstanceShutdownOrDeleteRequest();
            cloudDatasourceDelReq.setId(response.getMetadata().getId());
            kafkaTemplate.send("clouddatasource-instance-config-delete-requests", cloudDatasourceDelReq);
        }
    }

    public HttpStatus shutdownInstance(UUID projectId, UUID instanceId) {
        if (instanceRepository.findByProjectIdAndId(projectId, instanceId) != null) {
            InstanceShutdownOrDeleteRequest shutdownRequest = new InstanceShutdownOrDeleteRequest();
            shutdownRequest.setId(instanceId);
            kafkaTemplate.send("orchestrator-instance-shutdown-requests", shutdownRequest);
            return HttpStatus.OK;
        } else {
            return HttpStatus.NOT_FOUND;
        }
    }

    @KafkaListener(id = "orchestrator-instance-shutdown-responses", topics = "orchestrator-instance-shutdown-responses")
    public void handleShutdownInstanceResponse(OrchestratorResponse orchestratorResponse) {
        if (orchestratorResponse.getAck()) {
            Optional<Instance> optionalInstance = instanceRepository.findById(orchestratorResponse.getMetadata().getId());
            if (optionalInstance.isPresent()) {
                Instance instance = optionalInstance.get();
                instance.setState("shutdown");
                instanceRepository.save(instance);
            }
        } else {
            log.info("**** Failed to shutdown instance: instance.id={}", orchestratorResponse.getMetadata().getId());
        }
    }

    public HttpStatus deleteInstance(UUID projectId, UUID instanceId) {
        if (instanceRepository.findByProjectIdAndId(projectId, instanceId) != null) {
            InstanceShutdownOrDeleteRequest deleteRequest = new InstanceShutdownOrDeleteRequest();
            deleteRequest.setId(instanceId);
            kafkaTemplate.send("orchestrator-instance-delete-requests", deleteRequest);
            return HttpStatus.OK;
        } else {
            return HttpStatus.NOT_FOUND;
        }
    }

    @KafkaListener(id = "orchestrator-instance-delete-responses", topics = "orchestrator-instance-delete-responses")
    public void handleDeleteResponse(OrchestratorResponse orchestratorResponse) {
        if (orchestratorResponse.getAck()) {
            log.info("Instance delete response: {}", orchestratorResponse.getMetadata().getId());
            Optional<Instance> optionalInstance = instanceRepository.findById(orchestratorResponse.getMetadata().getId());
            if (optionalInstance.isPresent()) {
                Instance instance = optionalInstance.get();
                processingStatusRepository.delete(instance.getStatus());
                instanceRepository.delete(instance);
            }
        } else {
            log.info("**** Failed to delete instance: instance.id={}", orchestratorResponse.getMetadata().getId());
        }
    }

    public void sendInstanceConfig(UUID instanceId, String instanceConfig) {
        log.info("**** Instance config: {}", instanceConfig);
        Map<String, String> cloudDatasourceMessage = new HashMap<>();
        cloudDatasourceMessage.put("instanceId", instanceId.toString());
        cloudDatasourceMessage.put("instanceConfig", instanceConfig);
        kafkaTemplate.send("clouddatasource-instance-config-save-requests", cloudDatasourceMessage);
    }

}
