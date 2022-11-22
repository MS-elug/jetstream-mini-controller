package com.amadeus.nats.jetstream.admin.comparators;

import com.amadeus.nats.jetstream.admin.model.StreamConfig;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class StreamConfigComparator {

  private StreamConfigComparator() {
    // no instantiation
  }

  public static List<StreamConfiguration> getStreamsToDelete(List<StreamInfo> currentConfig,
      List<StreamConfig> streamConfigs) {
    List<StreamConfiguration> streamsToDelete = new ArrayList<>();

    for (StreamInfo streamInfo : currentConfig) {
      if (!isStreamPresentInNewConfig(streamConfigs, streamInfo.getConfiguration().getName())) {
        streamsToDelete.add(streamInfo.getConfiguration());
      }
    }

    return streamsToDelete;
  }

  public static List<StreamConfiguration> getStreamsToCreate(List<StreamInfo> currentConfig,
      List<StreamConfig> streamConfigs) {
    List<StreamConfiguration> streamsToCreate = new ArrayList<>();

    for (StreamConfig streamConfig : streamConfigs) {
      if (!isStreamPresentInCurrentConfig(currentConfig, streamConfig.getStreamName())) {
        streamsToCreate.add(toStreamConfig(streamConfig));
      }
    }
    return streamsToCreate;
  }

  public static List<StreamConfiguration> compareStreamsWithConfig(List<StreamInfo> currentConfig,
      List<StreamConfig> streamConfigs) {
    List<StreamConfiguration> streamsToUpdate = new ArrayList<>();

    for (StreamConfig streamConfig : streamConfigs) {
      if (shouldbeUpdated(streamConfig, currentConfig)) {
        streamsToUpdate.add(toStreamConfig(streamConfig));
      }
    }

    return streamsToUpdate;
  }

  private static StreamConfiguration toStreamConfig(StreamConfig streamConfig) {
    return StreamConfiguration.builder()
        .name(streamConfig.getStreamName())
        .addSubjects(streamConfig.getSubjects())
        .replicas(streamConfig.getReplicas())
        .retentionPolicy(RetentionPolicy.get(streamConfig.getRetention()))
        .storageType(StorageType.get(streamConfig.getStorage()))
        .maxAge(Duration.ofSeconds(streamConfig.getMaxAge()))
        .build();
  }

  private static boolean shouldbeUpdated(StreamConfig stream, List<StreamInfo> currentConfig) {
    StreamInfo streamInfo = getStreamWithName(stream.getStreamName(), currentConfig);

    boolean shouldBeUpdated = false;

    if (streamInfo != null && !isEqual(streamInfo, stream)) {
      shouldBeUpdated = true;
    }

    return shouldBeUpdated;
  }

  private static boolean isEqual(StreamInfo streamInfo, StreamConfig stream) {
    StreamConfiguration streamConfigFromConfig = toStreamConfig(stream);
    StreamConfiguration streamConfigFromServer = streamInfo.getConfiguration();
    return streamConfigFromServer.getMaxAge().equals(streamConfigFromConfig.getMaxAge())
        && streamConfigFromServer.getSubjects().equals(streamConfigFromConfig.getSubjects())
        && streamConfigFromServer.getReplicas() == streamConfigFromConfig.getReplicas()
        && streamConfigFromServer.getStorageType().equals(streamConfigFromConfig.getStorageType())
        && streamConfigFromServer.getRetentionPolicy().equals(streamConfigFromConfig.getRetentionPolicy());
  }

  private static StreamInfo getStreamWithName(String streamName, List<StreamInfo> existingStreams) {
    return existingStreams.stream()
        .filter(stream -> streamName.equalsIgnoreCase(stream.getConfiguration().getName()))
        .findFirst()
        .orElse(null);
  }

  private static boolean isStreamPresentInNewConfig(List<StreamConfig> streamConfigs, String streamName) {
    return streamConfigs.stream()
        .filter(currentStream -> streamName.equalsIgnoreCase(currentStream.getStreamName()))
        .findFirst()
        .isPresent();
  }

  private static boolean isStreamPresentInCurrentConfig(List<StreamInfo> currentConfig, String streamName) {
    return currentConfig.stream()
        .filter(currentStream -> streamName.equalsIgnoreCase(currentStream.getConfiguration().getName()))
        .findFirst()
        .isPresent();
  }

}
