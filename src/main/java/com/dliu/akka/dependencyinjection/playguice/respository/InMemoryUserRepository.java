package com.dliu.akka.dependencyinjection.playguice.respository;

import com.dliu.akka.dependencyinjection.playguice.domain.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    private static final Map<String, String> users = new HashMap<String, String>(){{
       put("david", "this is the user details of david");
    }};

    @Override
    public String retrieve(String name) {
        return users.get(name);
    }
}
