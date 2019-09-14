package com.dliu.akka.lab.requestreplydemo.actors;

import java.util.Objects;
import java.util.UUID;

public class BackendProtocol {
    public static class ExecuteCommandCmd {
        public final UUID correlationId;
        public final String command;

        public ExecuteCommandCmd(UUID correlationId, String command) {
            this.correlationId = correlationId;
            this.command = command;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecuteCommandCmd that = (ExecuteCommandCmd) o;
            return Objects.equals(correlationId, that.correlationId) &&
                    Objects.equals(command, that.command);
        }

        @Override
        public int hashCode() {
            return Objects.hash(correlationId, command);
        }
    }

    public static class ExecuteCommandResponse {
        public final UUID correlationId;
        public final String result;

        public ExecuteCommandResponse(UUID correlationId, String result) {
            this.correlationId = correlationId;
            this.result = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecuteCommandResponse that = (ExecuteCommandResponse) o;
            return Objects.equals(correlationId, that.correlationId) &&
                    Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(correlationId, result);
        }

        @Override
        public String toString() {
            return "ExecuteCommandResponse{" +
                    "correlationId=" + correlationId +
                    ", result='" + result + '\'' +
                    '}';
        }
    }
}
