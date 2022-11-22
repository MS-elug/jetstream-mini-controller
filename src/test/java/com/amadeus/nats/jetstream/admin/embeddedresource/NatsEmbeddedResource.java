package com.amadeus.nats.jetstream.admin.embeddedresource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import np.com.madanpokharel.embed.nats.EmbeddedNatsConfig;
import np.com.madanpokharel.embed.nats.EmbeddedNatsServer;
import np.com.madanpokharel.embed.nats.NatsServerConfig;
import np.com.madanpokharel.embed.nats.NatsVersion;
import np.com.madanpokharel.embed.nats.ServerType;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class NatsEmbeddedResource implements QuarkusTestResourceLifecycleManager {

  private static final String FILE_TMP_NATS = "/tmp/nats/jetstream";

  private EmbeddedNatsServer natsServer;

  private final EmbeddedNatsConfig config;

  NatsUser natsUser;

  @Inject
  public NatsEmbeddedResource() {
    this.config = new EmbeddedNatsConfig.Builder()
        .withNatsServerConfig(
            new NatsServerConfig.Builder()
                .withServerType(ServerType.NATS)
                .withNatsVersion(new NatsVersion("v2.6.1"))
                .withPort(7656)
                .withHost("127.0.0.1")
                .withConfigParam("-js", "-js")
                .build())
        .build();
  }

  @Inject
  public NatsEmbeddedResource(@ConfigProperty(name = "nats.host", defaultValue = "127.0.0.1") String host,
      @ConfigProperty(name = "nats.port", defaultValue = "7656") String port) {
    this.config = new EmbeddedNatsConfig.Builder()
        .withNatsServerConfig(
            new NatsServerConfig.Builder()
                .withServerType(ServerType.NATS)
                .withPort(Integer.valueOf(port))
                .withConfigParam("-js", "-js")
                .withHost(host)
                .build())
        .build();
  }

  @Override
  public Map<String, String> start() {

    // clean existing data in jetstream folder
    try {
      FileUtils.deleteDirectory(new File(FILE_TMP_NATS));
    } catch (final IOException e) {
      log.error("Error occured during cleanup", e);
    }
    this.natsServer = new EmbeddedNatsServer(this.config);
    try {
      this.natsServer.startServer();
      this.natsUser = new NatsUser(this.natsServer.getNatsUrl());
      this.natsUser.afterPropertiesSet();
    } catch (final Exception e) {
      log.error("Error starting nats embedded server." + e);
    }
    return Map.of("nats.service.uri", this.natsServer.getNatsUrl() + ":" + this.natsServer.getRunningPort());
  }

  @Override
  public void stop() {
    if (this.natsUser != null) {
      this.natsUser.destroy();
    }
    if (this.natsServer != null) {
      this.natsServer.stopServer();
    }
    while (this.natsServer != null && this.natsServer.isServerRunning()) {
      // wait
    }
    // clean existing data in jetstream folder
    try {
      FileUtils.deleteDirectory(new File(FILE_TMP_NATS));
    } catch (final IOException e) {
      log.error("Error occured during cleanup", e);
    }
  }

  @Override
  public void inject(Object testInstance) {
    boolean injected = false;
    try {
      final Set<Field> natsEmbeddedBrokerFields = this.getFieldsOfClass(testInstance.getClass(), NatsUser.class,
          true);
      for (final Field field : natsEmbeddedBrokerFields) {
        field.setAccessible(true);
        field.set(testInstance, this.natsUser);
        injected = true;
      }
    } catch (final IllegalAccessException e) {
      // Bug: if multiple QuarkusTest, the QuarkusTestResource leaks across all tests.
      log.warn("Could not inject NatsMockUser into {}", testInstance.getClass(), e);
    }
    if (!injected) {
      log.warn("Could not inject NatsMockUser into {}, does it declare an EmbeddedKafkaBroker field ?",
          testInstance.getClass());
    }
  }

  private Set<Field> getFieldsOfClass(Class<?> klazz, Class<?> type, boolean isInstanceClass) {
    if (Object.class.equals(klazz)) {
      return new HashSet<>();
    }
    final Set<Field> set = this.getFieldsOfClass(klazz.getSuperclass(), type, false);
    for (final Field field : klazz.getDeclaredFields()) {
      if (field.getType().isAssignableFrom(type)
          && (isInstanceClass || !Modifier.isPrivate(field.getModifiers()))) {
        set.add(field);
      }
    }
    return set;
  }
}
