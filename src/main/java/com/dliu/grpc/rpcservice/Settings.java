package com.dliu.grpc.rpcservice;

import com.typesafe.config.Config;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.ExtensionId;
import akka.actor.ExtensionIdProvider;

public class Settings extends AbstractExtensionId<Settings.SettingsImpl> implements ExtensionIdProvider {

    public static final Settings SettingProvider = new Settings();

    private Settings() {
    }

    @Override
    public SettingsImpl createExtension(ExtendedActorSystem system) {
        return new SettingsImpl(system.settings().config());
    }

    @Override
    public ExtensionId<? extends Extension> lookup() {
        return Settings.SettingProvider;
    }

    public static class SettingsImpl implements Extension {
        public final String GRPC_SERVER_HOST;
        public final int GRPC_SERVER_PORT;

        public SettingsImpl(Config config) {
            Config grpcServerConfig = config.getConfig("grpc.server");
            GRPC_SERVER_HOST = grpcServerConfig.getString("host");
            GRPC_SERVER_PORT = grpcServerConfig.getInt("port");
        }
    }

}