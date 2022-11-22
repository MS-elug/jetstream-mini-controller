package com.amadeus.nats.jetstream.admin.comparators;

import com.amadeus.nats.jetstream.admin.model.KeyValueConfig;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.StorageType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class KeyValueConfigComparator {

  private KeyValueConfigComparator() {
    // no instantiation
  }


  public static List<KeyValueConfiguration> getKVToDelete(List<KeyValueConfiguration> currentConfigs, List<KeyValueConfig> keyValueConfigs) {
    List<KeyValueConfiguration> keyValuesToDelete = new ArrayList<>();

    for (KeyValueConfiguration keyValueConfig : currentConfigs) {
      if (!isKeyValuePresentInNewConfig(keyValueConfigs, keyValueConfig.getBucketName())) {
        keyValuesToDelete.add(keyValueConfig);
      }
    }

    return keyValuesToDelete;
  }

  public static List<KeyValueConfiguration> compareKVWithConfig(List<KeyValueConfiguration> currentConfigs, List<KeyValueConfig> keyValueConfigs) {

    List<KeyValueConfiguration> keyValuesToUpdate = new ArrayList<>();

    for (KeyValueConfig keyValueConfig : keyValueConfigs) {
      if (shouldbeUpdated(keyValueConfig, currentConfigs)) {
        keyValuesToUpdate.add(toKeyValueConfig(keyValueConfig));
      }
    }

    return keyValuesToUpdate;

  }

  public static List<KeyValueConfiguration> getKVToCreate(List<KeyValueConfiguration> currentConfigs, List<KeyValueConfig> keyValueConfigs) {
    List<KeyValueConfiguration> keyValuesToCreate = new ArrayList<>();

    for (KeyValueConfig keyValueConfig : keyValueConfigs) {
      if (!isKeyValuePresentInCurrentConfig(currentConfigs, keyValueConfig.getName())) {
        keyValuesToCreate.add(toKeyValueConfig(keyValueConfig));
      }
    }
    return keyValuesToCreate;
  }

  private static boolean isKeyValuePresentInCurrentConfig(List<KeyValueConfiguration> keyValuesConfigs, String bucketName) {
    return keyValuesConfigs.stream()
        .filter(currentKeyValue-> bucketName.equalsIgnoreCase(currentKeyValue.getBucketName()))
        .findFirst()
        .isPresent();
  }

  private static boolean isKeyValuePresentInNewConfig(List<KeyValueConfig> keyValuesConfigs, String bucketName) {
    return keyValuesConfigs.stream()
        .filter(currentKeyValue -> bucketName.equalsIgnoreCase(currentKeyValue.getName()))
        .findFirst()
        .isPresent();
  }

  private static boolean shouldbeUpdated(KeyValueConfig keyValueConfig, List<KeyValueConfiguration> currentConfigs) {
    KeyValueConfiguration keyValueConfigFromServer = getKeyValueWithName(keyValueConfig.getName(), currentConfigs);

    boolean shouldBeUpdated = false;

    if (keyValueConfigFromServer != null && !isEqual(keyValueConfigFromServer, keyValueConfig)) {
      shouldBeUpdated = true;
    }

    return shouldBeUpdated;
  }

  private static boolean isEqual(KeyValueConfiguration keyValueConfigFromServer, KeyValueConfig keyValueConfig) {
    KeyValueConfiguration keyValueConfigFromConfig = toKeyValueConfig(keyValueConfig);
    return keyValueConfigFromServer.getTtl() == keyValueConfigFromConfig.getTtl()
        && keyValueConfigFromServer.getReplicas() == keyValueConfigFromConfig.getReplicas()
        && keyValueConfigFromServer.getStorageType().equals(keyValueConfigFromConfig.getStorageType())
        && keyValueConfigFromServer.getMaxHistoryPerKey() == keyValueConfigFromConfig.getMaxHistoryPerKey();
  }

  private static KeyValueConfiguration getKeyValueWithName(String keyValueName, List<KeyValueConfiguration> existingKeyValues) {
    return existingKeyValues.stream()
        .filter(existingKeyValue -> keyValueName.equalsIgnoreCase(existingKeyValue.getBucketName()))
        .findFirst()
        .orElse(null);
  }

  private static KeyValueConfiguration toKeyValueConfig(KeyValueConfig keyValueConfig) {
    return KeyValueConfiguration.builder()
        .name(keyValueConfig.getName())
        .storageType(StorageType.get(keyValueConfig.getStorage()))
        .replicas(keyValueConfig.getReplicas())
        .ttl(Duration.ofSeconds(keyValueConfig.getTimeToLiveInSeconds()))
        .maxHistoryPerKey(keyValueConfig.getMaxHistoryPerKey())
        .build();
  }
}
