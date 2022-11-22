package com.amadeus.nats.jetstream.admin;

import com.amadeus.nats.jetstream.admin.embeddedresource.NatsEmbeddedResource;
import com.amadeus.nats.jetstream.admin.embeddedresource.NatsUser;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(NatsEmbeddedResource.class)
class JetstreamUpdatesSchedulerQuarkusTest {

  NatsUser user;

  @BeforeEach
  public void setUp() throws IOException, JetStreamApiException {
    // create some streams already

    StreamConfiguration streamConfig1 = StreamConfiguration.builder()
        .name("stream1")
        .addSubjects(Arrays.asList("subject1"))
        .replicas(1)
        .retentionPolicy(RetentionPolicy.Limits)
        .maxAge(Duration.ofMinutes(60))
        .storageType(StorageType.Memory)
        .build();

    StreamConfiguration streamConfig2 = StreamConfiguration.builder()
        .name("stream2")
        .addSubjects(Arrays.asList("subject10", "subject11", "subject12"))
        .replicas(1)
        .retentionPolicy(RetentionPolicy.Limits)
        .maxAge(Duration.ofMinutes(5))
        .storageType(StorageType.Memory)
        .build();

    KeyValueConfiguration keyValue1 = KeyValueConfiguration.builder()
        .name("bucket1")
        .replicas(1)
        .maxHistoryPerKey(20)
        .ttl(Duration.ofSeconds(200))
        .storageType(StorageType.Memory)
        .build();

    KeyValueConfiguration keyValue2 = KeyValueConfiguration.builder()
        .name("bucket2")
        .replicas(1)
        .maxHistoryPerKey(15)
        .ttl(Duration.ofSeconds(150))
        .storageType(StorageType.File)
        .build();

    KeyValueConfiguration keyValue4 = KeyValueConfiguration.builder()
        .name("bucket4")
        .replicas(1)
        .maxHistoryPerKey(2)
        .ttl(Duration.ofSeconds(15))
        .storageType(StorageType.File)
        .build();

    while (this.user == null || this.user.getNatsConnection() == null) {
      // wait for connection to be started
    }
    this.user.getNatsConnection().jetStreamManagement().addStream(streamConfig1);

    this.user.getNatsConnection().jetStreamManagement().addStream(streamConfig2);

    this.user.getNatsConnection().keyValueManagement().create(keyValue1);

    this.user.getNatsConnection().keyValueManagement().create(keyValue2);

    this.user.getNatsConnection().keyValueManagement().create(keyValue4);
  }

  @AfterEach
  public void tearDown() throws IOException, JetStreamApiException {
    // clean all streams
    this.user.getNatsConnection()
    .jetStreamManagement()
    .getStreams()
    .forEach(stream -> {
      try {
        this.user.getNatsConnection()
        .jetStreamManagement()
        .deleteStream(stream.getConfiguration().getName());
      } catch (IOException | JetStreamApiException e) {
        e.printStackTrace();
      }
    });
    // clean all keyvalues
    this.user.getNatsConnection()
        .keyValueManagement()
        .getStatuses()
        .forEach(keyValueStatus -> {
          try {
            this.user.getNatsConnection()
                .keyValueManagement()
                .delete(keyValueStatus.getConfiguration().getBucketName());
          } catch (IOException | JetStreamApiException e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  void testStreamManagementProcess() throws IOException, JetStreamApiException, InterruptedException {

    Thread.sleep(5000);

    List<StreamInfo> streams = this.user.getNatsConnection()
        .jetStreamManagement()
        .getStreams()
        .stream().filter(streamInfo -> !streamInfo.getConfiguration().getName().startsWith("KV_"))
        .collect(Collectors.toList());

    List<KeyValueConfiguration> keyValues = this.user.getNatsConnection()
        .keyValueManagement()
        .getStatuses()
        .stream()
        .map(status -> status.getConfiguration())
        .collect(Collectors.toList());

    assertEquals(3, streams.size());
    for (StreamInfo stream : streams) {
      assertTrue("stream1".equals(stream.getConfiguration().getName())
          || "stream3".equals(stream.getConfiguration().getName())
          || "stream4".equals(stream.getConfiguration().getName()));
    }

    assertEquals(3, keyValues.size());
    for (KeyValueConfiguration keyValue : keyValues) {
      assertTrue("bucket1".equals(keyValue.getBucketName())
          || "bucket2".equals(keyValue.getBucketName())
          || "bucket3".equals(keyValue.getBucketName()));
    }

  }

}
