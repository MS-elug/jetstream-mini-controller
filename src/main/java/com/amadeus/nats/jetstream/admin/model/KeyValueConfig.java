package com.amadeus.nats.jetstream.admin.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeyValueConfig {

  private String name;

  private String storage;

  private int replicas;

  private int maxHistoryPerKey;

  private int timeToLiveInSeconds;

  @Override
  public String toString() {
    return "KeyValueConfig{" +
        "name='" + name + '\'' +
        ", storage='" + storage + '\'' +
        ", replicas=" + replicas +
        ", maxHistoryPerKey=" + maxHistoryPerKey +
        ", timeToLiveInSeconds=" + timeToLiveInSeconds +
        '}';
  }
}
