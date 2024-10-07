package com.onpremisecloudimplementation.vmorchestrator.vmorchestratorgrpcclient;

import org.springframework.stereotype.Service;

import com.onpremisecloudimplementation.grpcservices.HeartbeatRequest;
import com.onpremisecloudimplementation.grpcservices.HeartbeatResponse;
import com.onpremisecloudimplementation.grpcservices.HeartbeatServiceGrpc;
import com.onpremisecloudimplementation.grpcservices.HeartbeatServiceGrpc.HeartbeatServiceBlockingStub;
import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;



@Service
public class HeartbeatClient {
    
    public Boolean sendHeartbeatRequest(ComputeNode targetServer) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetServer.getGrpcUrlString()).usePlaintext().build();
        HeartbeatServiceBlockingStub stub = HeartbeatServiceGrpc.newBlockingStub(channel);
        HeartbeatResponse heartbeatResponse;

        try {       
            heartbeatResponse = stub.checkHeartbeat(HeartbeatRequest.newBuilder().build());
        } catch (StatusRuntimeException e) {
            heartbeatResponse = HeartbeatResponse.newBuilder().setAck(false).build();
        } finally {
            channel.shutdownNow();
        }

        return heartbeatResponse.getAck();
    }

}