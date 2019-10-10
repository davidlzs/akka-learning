package com.dliu.akka.dependencyinjection.playguice.respository;

import com.dliu.akka.dependencyinjection.playguice.domain.User;
import com.dliu.akka.dependencyinjection.playguice.domain.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    private static final Map<String, User> users = new HashMap<String, User>(){{
       put("david", new User("david", 10, "Soccer"));
    }};

    @Override
    public User retrieve(String name) {
        return users.get(name);
    }
}
