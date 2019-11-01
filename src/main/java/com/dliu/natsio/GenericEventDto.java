package com.dliu.natsio;

import java.time.Instant;
import java.util.Objects;

public class GenericEventDto {
    public String id;
    public String source;
    public String type;
    public Instant createdAt;
    public String body;

    public GenericEventDto(String id, String source, String type, Instant createdAt, String body) {
        this.id = id;
        this.source = source;
        this.type = type;
        this.createdAt = createdAt;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericEventDto that = (GenericEventDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(source, that.source) &&
                Objects.equals(type, that.type) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, type, createdAt, body);
    }

    @Override
    public String toString() {
        return "GenericEventDto{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", body='" + body + '\'' +
                '}';
    }
}
