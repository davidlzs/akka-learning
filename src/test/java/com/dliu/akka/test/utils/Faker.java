package com.dliu.akka.test.utils;

import java.util.UUID;

public class Faker {
    private static final String UUID_FORMAT = "00000000-0000-0000-0000-%012d";
    public static UUID fakeId() {
        return UUID.randomUUID();
    }

    public static UUID fakeId(int sequence) {
        return UUID.fromString(String.format(UUID_FORMAT, sequence));
    }
}
