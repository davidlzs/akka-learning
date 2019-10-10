package com.dliu.akka.dependencyinjection.playguice.domain;

public interface UserRepository {
    User retrieve(String name);
}
