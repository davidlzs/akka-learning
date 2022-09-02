package com.dliu.killrweather;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import akka.actor.AddressFromURIString;
import akka.actor.typed.ActorSystem;

public class KillrWeather {
    /*
     * Start the Akka cluster in the same JVM, 2 seed nodes and one extra non-seed node
     * 1. need to grab the seed node ports from the configuration.
     * 2. then add a "0" - which means os decided port for the non-seed node
     * 3. loop through all the ports, for any port > 0 then offset it with 10000, to calculate the httpport; for "0" we just leave it as "0" to let os decide
     * 4. then configure the akka remote artery port to be port and start http server with the calculated http ports
     * 5. start the 3 actors systems
     */
    public static void main(String[] args) {

        List<Integer> seedNodePorts = ConfigFactory.load("killr_weather_application.conf").getStringList("akka.cluster.seed-nodes")
                .stream()
                .map(AddressFromURIString::parse)
                .map(addr -> (Integer) addr.port().get())
                .collect(Collectors.toList());

        ArrayList<Integer> portsAndZero = new ArrayList<>(seedNodePorts);
        portsAndZero.add(0);

        for (int port : portsAndZero) {
            final int httpPort;
            if (port > 0) httpPort = 10000 + port;
            else httpPort = 0; // let OS decide

            Config config = configWithPort(port);
            ActorSystem.create(Guardian.create(httpPort), "KillrWeather", config);
        }
    }

    private static Config configWithPort(int port) {
        return ConfigFactory.parseMap(
                Collections.singletonMap("akka.remote.artery.canonical.port", Integer.toString(port))
        ).withFallback(ConfigFactory.load("killr_weather_application.conf"));
    }
}
