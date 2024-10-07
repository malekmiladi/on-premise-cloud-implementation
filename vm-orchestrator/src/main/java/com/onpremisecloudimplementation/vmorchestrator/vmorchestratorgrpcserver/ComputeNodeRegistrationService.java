package com.onpremisecloudimplementation.vmorchestrator.vmorchestratorgrpcserver;

import java.util.UUID;

import com.onpremisecloudimplementation.grpcservices.ComputeNodeRegistrationServiceGrpc;
import com.onpremisecloudimplementation.grpcservices.OrchestratorAck;
import com.onpremisecloudimplementation.grpcservices.ComputeNodeRegisterationData;
import com.onpremisecloudimplementation.vmorchestrator.computenode.ComputeNodeService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ComputeNodeRegistrationService extends ComputeNodeRegistrationServiceGrpc.ComputeNodeRegistrationServiceImplBase {
    
    private final ComputeNodeService computeNodeService;

    @Override
    public void registerComputeNode(ComputeNodeRegisterationData registrationRequest, StreamObserver<OrchestratorAck> responseObserver) {
        OrchestratorAck.Builder response = OrchestratorAck.newBuilder();
        UUID computeNodeId = computeNodeService.registerComputeNode(registrationRequest);
        if (computeNodeId != null) {
            log.info(
                "**** Registered node: uuid={} grpcUrlString={} defaultMemoryGb={} defaultStorageGb={} defaultCpus={}",
                    computeNodeId,
                    registrationRequest.getGrpcUrlString(),
                    registrationRequest.getDefaultMemoryGb(),
                    registrationRequest.getDefaultStorageGb(),
                    registrationRequest.getDefaultCpus()
            );
            response.setAck(true)
                    .setComputeNodeId(computeNodeId.toString())
                    .setMessage("Registered compute node. computeNode.uuid=" + computeNodeId);
        } else {
            log.info("**** Compute node already registered: computeNode.uuid={}", registrationRequest.getNodeId());
            response.setComputeNodeId(registrationRequest.getNodeId())
                    .setAck(false)
                    .setMessage("Compute node already registered. computeNode=" + registrationRequest.getNodeId());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}
