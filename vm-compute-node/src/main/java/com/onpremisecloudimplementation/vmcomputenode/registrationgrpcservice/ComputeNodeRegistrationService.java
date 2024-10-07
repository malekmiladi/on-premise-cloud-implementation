package com.onpremisecloudimplementation.vmcomputenode.registrationgrpcservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import com.onpremisecloud.grpcservices.ComputeNodeRegistrationData;
import com.onpremisecloud.grpcservices.OrchestratorAck;
import com.onpremisecloud.grpcservices.ComputeNodeRegistrationServiceGrpc.ComputeNodeRegistrationServiceBlockingStub;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@Slf4j
@PropertySource("classpath:instance.properties")
public class ComputeNodeRegistrationService implements CommandLineRunner {

    @Value("${on-premise-cloud.compute-node.etc.instance-id.file-path}")
    private String APP_ID_FILE_PATH;

    @Value("${on-premise-cloud.compute-node.default-cpus}")
    private int DEFAULT_CPUS;

    @Value("${on-premise-cloud.compute-node.default-memory-gb}")
    private int DEFAULT_MEMORY_GB;

    @Value("${on-premise-cloud.compute-node.default-storage-gb}")
    private int DEFAULT_STORAGE_GB;

    @Value("${on-premise-cloud.compute-node.region}")
    private String REGION;

    @GrpcClient("vm-orchestrator-grpc-server")
    private ComputeNodeRegistrationServiceBlockingStub STUB;

    @Override
    public void run(String... args) throws Exception {
        registerServer();
    }

    private void registerServer() throws InterruptedException {
        boolean registered = false;
        while (!registered) {
            try {
                registered = sendRegistrationRequest();
            } catch (StatusRuntimeException e) {
                log.error("Orchestrator service offline. Retrying in 10s...");
                Thread.sleep(Duration.ofSeconds(10));
            } catch (IOException e) {
                log.error("Failed to read id from file. {}", String.valueOf(e));
            }
        }
    }

    public boolean sendRegistrationRequest() throws StatusRuntimeException, IOException {

        boolean registered = false;
        File appIdFile = new File(APP_ID_FILE_PATH);
        String appId = Files.readString(appIdFile.toPath(), StandardCharsets.UTF_8);

        if (appId.isEmpty()) {
            ComputeNodeRegistrationData instanceConfig = ComputeNodeRegistrationData.newBuilder()
                    .setNodeId("")
                    .setDefaultCpus(DEFAULT_CPUS)
                    .setDefaultMemoryGb(DEFAULT_MEMORY_GB)
                    .setDefaultStorageGb(DEFAULT_STORAGE_GB)
                    .setRegion(REGION)
                    .setGrpcUrlString(Inet4Address.getLocalHost().getHostAddress() + ":9090")
                    .build();

            OrchestratorAck registrationResponse = STUB.registerComputeNode(instanceConfig);

            if (registrationResponse.getAck()) {
                log.info("[REGISTRATION SUCCESSFUL] compute-node.id={}", registrationResponse.getComputeNodeId());
                FileWriter fileWriter = new FileWriter(appIdFile.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                try {
                    bufferedWriter.write(registrationResponse.getComputeNodeId());
                    bufferedWriter.close();
                    registered = true;
                } catch (Exception e) {
                    log.error("Failed to write compute-node.id={} to {}, {}", appId, appIdFile.getAbsolutePath(), e.getMessage());
                }
            } else {
                log.error("[REGISTRATION FAILED] {}", registrationResponse.getMessage());
            }
        } else {
            log.info("This compute node is already registered. compute-node.id={}", appId);
            registered = true;
        }
        return registered;
    }

}
