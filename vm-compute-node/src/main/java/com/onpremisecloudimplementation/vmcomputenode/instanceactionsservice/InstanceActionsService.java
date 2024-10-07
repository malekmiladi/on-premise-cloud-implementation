package com.onpremisecloudimplementation.vmcomputenode.instanceactionsservice;

import com.onpremisecloud.grpcservices.InstanceActionsServiceGrpc;
import com.onpremisecloud.grpcservices.InstanceSpecifications;
import com.onpremisecloud.grpcservices.ComputeNodeResponse;
import com.onpremisecloud.grpcservices.InstanceShutdownOrDeleteTarget;
import com.onpremisecloudimplementation.vmcomputenode.instance.InstanceService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class InstanceActionsService extends InstanceActionsServiceGrpc.InstanceActionsServiceImplBase {
    
    private final InstanceService instanceService;

    @Override
    public void createNewInstance(InstanceSpecifications newInstanceRequest, StreamObserver<ComputeNodeResponse> responseObserver) {
        ComputeNodeResponse response = instanceService.createVirtualMachine(newInstanceRequest);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteInstance(InstanceShutdownOrDeleteTarget deleteRequest, StreamObserver<ComputeNodeResponse> responseObserver) {
        ComputeNodeResponse response = instanceService.deleteVirtualMachine(deleteRequest.getInstanceId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void shutDownInstance(InstanceShutdownOrDeleteTarget shutdownOrDeleteRequest, StreamObserver<ComputeNodeResponse> responseObserver) {
        ComputeNodeResponse response = instanceService.shutdownVirtualMachine(shutdownOrDeleteRequest.getInstanceId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
