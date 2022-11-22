package com.amadeus.nats.jetstream.admin.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamConfig {

  private String streamName;

  private List<String> subjects;

  private String storage;

  private int replicas;

  private String retention;

  private long maxAge;

  @Override
  public String toString() {
    return "StreamConfig [streamName=" + streamName + ", subjects=" + subjects + ", storage=" + storage + ", replicas="
        + replicas + ", retention=" + retention + ", maxAge=" + maxAge + "]";
  }

}
