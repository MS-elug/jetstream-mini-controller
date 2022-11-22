package com.amadeus.nats.jetstream.admin;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.amadeus.nats.jetstream.admin.model.JetstreamConfigs;
import com.amadeus.nats.jetstream.admin.model.KeyValueConfig;
import io.nats.client.api.KeyValueConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amadeus.nats.jetstream.admin.model.StreamConfig;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

class JetstreamUpdatesSchedulerTest {

  private JetstreamUpdatesScheduler scheduler;

  private NatsConnection nc;

  private JetstreamConfigResolver resolver;

  @BeforeEach
  public void setUp() {

    this.nc = mock(NatsConnection.class);
    this.resolver = mock(JetstreamConfigResolver.class);

    this.scheduler = new JetstreamUpdatesScheduler(this.nc, this.resolver);
  }

  @Test
  void testSchedulerProcessWithSomethingToDo() throws IOException, JetStreamApiException {
    when(this.resolver.resolveConfig()).thenReturn(createConfigs());

    List<StreamInfo> existingStreams = createExistingStreamsConfig();
    when(this.nc.getCurrentStreamConfig()).thenReturn(existingStreams);

    List<KeyValueConfiguration> existingKeyValues = createExistingKeyValues();
    when(this.nc.getCurrentKeyValuesConfig()).thenReturn(existingKeyValues);

    this.scheduler.updateStreams();

    verify(this.nc, times(1)).createStreams(anyList());
    verify(this.nc, times(1)).updateStreams(anyList());
    verify(this.nc, times(1)).deleteStreams(anyList());
    verify(this.nc, times(1)).createKeyValues(anyList());
    verify(this.nc, times(1)).updateKeyValues(anyList());
    verify(this.nc, times(1)).deleteKeyValues(anyList());

  }

  @Test
  void testSchedulerProcessWithNothingToDo() throws IOException, JetStreamApiException {

    when(this.resolver.resolveConfig()).thenReturn(new JetstreamConfigs());
    when(this.nc.getCurrentStreamConfig()).thenReturn(new ArrayList<>());

    this.scheduler.updateStreams();

    verify(this.nc, times(1)).createStreams(Collections.emptyList());
    verify(this.nc, times(1)).updateStreams(Collections.emptyList());
    verify(this.nc, times(1)).deleteStreams(Collections.emptyList());
    verify(this.nc, times(1)).createKeyValues(Collections.emptyList());
    verify(this.nc, times(1)).updateKeyValues(Collections.emptyList());
    verify(this.nc, times(1)).deleteKeyValues(Collections.emptyList());
  }

  private JetstreamConfigs createConfigs() {
    JetstreamConfigs configs = new JetstreamConfigs();

    StreamConfig config1 = new StreamConfig();
    config1.setStreamName("stream1");
    config1.setMaxAge(10);
    config1.setReplicas(3);
    config1.setStorage("file");
    config1.setRetention("limits");
    config1.setSubjects(Arrays.asList("subject1", "subject2"));
    configs.getStreamConfigs().add(config1);

    StreamConfig config2 = new StreamConfig();
    config2.setStreamName("stream2");
    config2.setMaxAge(5);
    config2.setReplicas(2);
    config2.setStorage("memory");
    config1.setRetention("limits");
    config2.setSubjects(Arrays.asList("subject3", "subject4", "subject5"));
    configs.getStreamConfigs().add(config2);

    StreamConfig config3 = new StreamConfig();
    config3.setStreamName("stream3");
    config3.setMaxAge(60);
    config3.setReplicas(1);
    config3.setStorage("file");
    config3.setRetention("limits");
    config3.setSubjects(Arrays.asList("subject6", "subject7"));
    configs.getStreamConfigs().add(config3);

    StreamConfig config4 = new StreamConfig();
    config4.setStreamName("stream4");
    config4.setMaxAge(120);
    config4.setReplicas(1);
    config4.setStorage("file");
    config4.setRetention("limits");
    config4.setSubjects(Arrays.asList("subject8", "subject9"));
    configs.getStreamConfigs().add(config4);

    KeyValueConfig keyValueConfig1 = new KeyValueConfig();
    keyValueConfig1.setName("bucket1");
    keyValueConfig1.setReplicas(2);
    keyValueConfig1.setStorage("memory");
    keyValueConfig1.setMaxHistoryPerKey(10);
    keyValueConfig1.setTimeToLiveInSeconds(100);
    configs.getKeyValueConfigs().add(keyValueConfig1);

    KeyValueConfig keyValueConfig2 = new KeyValueConfig();
    keyValueConfig2.setName("bucket2");
    keyValueConfig2.setReplicas(3);
    keyValueConfig2.setStorage("file");
    keyValueConfig2.setMaxHistoryPerKey(15);
    keyValueConfig2.setTimeToLiveInSeconds(150);
    configs.getKeyValueConfigs().add(keyValueConfig2);

    KeyValueConfig keyValueConfig3 = new KeyValueConfig();
    keyValueConfig3.setName("bucket3");
    keyValueConfig3.setReplicas(3);
    keyValueConfig3.setStorage("memory");
    keyValueConfig3.setMaxHistoryPerKey(1);
    keyValueConfig3.setTimeToLiveInSeconds(5);
    configs.getKeyValueConfigs().add(keyValueConfig3);

    KeyValueConfig keyValueConfig4 = new KeyValueConfig();
    keyValueConfig4.setName("bucket4");
    keyValueConfig4.setReplicas(3);
    keyValueConfig4.setStorage("file");
    keyValueConfig4.setMaxHistoryPerKey(64);
    keyValueConfig4.setTimeToLiveInSeconds(1000);
    configs.getKeyValueConfigs().add(keyValueConfig4);

    return configs;
  }

  private List<StreamInfo> createExistingStreamsConfig() {
    List<StreamInfo> streams = new ArrayList<>();

    StreamInfo stream1 = mock(StreamInfo.class);
    StreamConfiguration streamConfig1 = StreamConfiguration.builder()
        .name("stream1")
        .addSubjects(Arrays.asList("subject1"))
        .replicas(2)
        .retentionPolicy(RetentionPolicy.Limits)
        .maxAge(Duration.ofMinutes(10))
        .storageType(StorageType.File)
        .build();
    when(stream1.getConfiguration()).thenReturn(streamConfig1);
    streams.add(stream1);

    StreamInfo stream2 = mock(StreamInfo.class);
    StreamConfiguration streamConfig2 = StreamConfiguration.builder()
        .name("stream2")
        .addSubjects(Arrays.asList("subject3", "subject4", "subject5"))
        .replicas(2)
        .retentionPolicy(RetentionPolicy.Limits)
        .maxAge(Duration.ofMinutes(5))
        .storageType(StorageType.Memory)
        .build();
    when(stream2.getConfiguration()).thenReturn(streamConfig2);
    streams.add(stream2);

    return streams;
  }

  private List<KeyValueConfiguration> createExistingKeyValues() {
    List<KeyValueConfiguration> keyValues =  new ArrayList<>();

    KeyValueConfiguration keyValue1 = KeyValueConfiguration.builder()
        .name("bucket1")
        .replicas(2)
        .maxHistoryPerKey(20)
        .ttl(Duration.ofSeconds(200))
        .storageType(StorageType.Memory)
        .build();
    keyValues.add(keyValue1);

    KeyValueConfiguration keyValue2 = KeyValueConfiguration.builder()
        .name("bucket2")
        .replicas(3)
        .maxHistoryPerKey(15)
        .ttl(Duration.ofSeconds(150))
        .storageType(StorageType.File)
        .build();
    keyValues.add(keyValue2);

    return keyValues;
  }

}
