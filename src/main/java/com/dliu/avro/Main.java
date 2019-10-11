package com.dliu.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class Main {
    public static void main(String[] args) {
        Schema clientIdentifier = SchemaBuilder.record("ClientIdentifier")
                .namespace("com.baeldung.avro")
                .fields().requiredString("hostName").requiredString("ipAddress")
                .endRecord();

        Schema avroHttpRequest = SchemaBuilder.record("AvroHttpRequest")
                .namespace("com.baeldung.avro")
                .fields().requiredLong("requestTime")
                .name("clientIdentifier")
                .type(clientIdentifier)
                .noDefault()
                .name("employeeNames")
                .type()
                .array()
                .items()
                .stringType()
                .arrayDefault(null)
                .name("active")
                .type()
                .enumeration("Active")
                .symbols("YES","NO")
                .noDefault()
                .endRecord();
    }
}
