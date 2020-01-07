package com.dliu.grpc.rpcservice;

import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RPCServiceImpl implements RPCService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServiceImpl.class);

    @Override
    public CompletionStage<ExecuteCommandResponse> executeCommand(ExecuteCommandRequest request) {

        LOGGER.info("Request - " + request.getRequestId());
        ExecuteCommandResponse reply = ExecuteCommandResponse.newBuilder()
                .setRequestId(request.getRequestId())
                .setValue(ByteString.copyFromUtf8("Got it"))
                .build();
        return CompletableFuture.completedFuture(reply);
    }
}
