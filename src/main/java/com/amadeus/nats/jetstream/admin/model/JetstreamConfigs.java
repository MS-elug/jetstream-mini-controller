package com.amadeus.nats.jetstream.admin.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JetstreamConfigs {

  private List<StreamConfig> streamConfigs = new ArrayList<>();

  private List<KeyValueConfig> keyValueConfigs = new ArrayList<>();

  @Override
  public String toString() {
    return "JetstreamConfigs{" +
        "streamConfigs=" + streamConfigs +
        ", keyValueConfigs=" + keyValueConfigs +
        '}';
  }
}
