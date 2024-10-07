package com.onpremisecloudimplementation.vmcomputenode.heartbeatgrpcservice;

import com.onpremisecloud.grpcservices.HeartbeatRequest;
import com.onpremisecloud.grpcservices.HeartbeatResponse;
import com.onpremisecloud.grpcservices.HeartbeatServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class HeartbeatGrpcService extends HeartbeatServiceGrpc.HeartbeatServiceImplBase {
    @Override
    public void checkHeartbeat(HeartbeatRequest heartbeatRequest, StreamObserver<HeartbeatResponse> responseObserver) {
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setAck(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
