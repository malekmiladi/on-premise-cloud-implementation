package com.onpremisecloudimplementation.vmorchestrator.vmorchestratorgrpcclient;

import org.springframework.stereotype.Service;

import com.onpremisecloudimplementation.grpcservices.InstanceActionsServiceGrpc;
import com.onpremisecloudimplementation.grpcservices.InstanceActionsServiceGrpc.InstanceActionsServiceBlockingStub;
import com.onpremisecloudimplementation.grpcservices.InstanceSpecifications;
import com.onpremisecloudimplementation.grpcservices.ComputeNodeResponse;
import com.onpremisecloudimplementation.grpcservices.InstanceShutdownOrDeleteTarget;
import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class InstanceActionsServiceClient {

    public ComputeNodeResponse sendCreateInstanceRequest(ComputeNode targetServer, InstanceSpecifications instanceSpecs) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetServer.getGrpcUrlString()).usePlaintext().build();
        InstanceActionsServiceBlockingStub stub = InstanceActionsServiceGrpc.newBlockingStub(channel);
        ComputeNodeResponse InstanceCreateResponse = stub.createNewInstance(instanceSpecs);

        channel.shutdownNow();

        return InstanceCreateResponse;
    }

    public ComputeNodeResponse sendInstanceShutdownRequest(ComputeNode targetNode, InstanceShutdownOrDeleteTarget shutdownRequest) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.getGrpcUrlString())
            .usePlaintext()
            .build();
        InstanceActionsServiceBlockingStub stub = InstanceActionsServiceGrpc.newBlockingStub(channel);
        ComputeNodeResponse ComputeNodeResponse = stub.shutDownInstance(shutdownRequest);

        channel.shutdownNow();

        return ComputeNodeResponse;
    }

    public ComputeNodeResponse sendInstanceDeleteRequest(ComputeNode targetNode, InstanceShutdownOrDeleteTarget deleteRequest) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.getGrpcUrlString())
            .usePlaintext()
            .build();
        InstanceActionsServiceBlockingStub stub = InstanceActionsServiceGrpc.newBlockingStub(channel);
        ComputeNodeResponse ComputeNodeResponse = stub.deleteInstance(deleteRequest);

        channel.shutdownNow();

        return ComputeNodeResponse;
    }

}
