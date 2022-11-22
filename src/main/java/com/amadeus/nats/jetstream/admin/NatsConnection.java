package com.amadeus.nats.jetstream.admin;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class NatsConnection {

  private Connection nc;

  @Inject
  public NatsConnection(@ConfigProperty(name = "nats.url") String natsUrl) throws IOException, InterruptedException {
    Options options = new Options.Builder()
        .servers(natsUrl.split(","))
        .build();
    this.nc = Nats.connect(options);
  }

  public void createStreams(List<StreamConfiguration> streamsToCreate) {

    for (StreamConfiguration streamConf : streamsToCreate) {
      try {
        log.info("Performing stream creation for {}", streamConf.getName());
        this.nc.jetStreamManagement().addStream(streamConf);
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to create stream with info: " + streamConf, e);
      }
    }
  }

  public void updateStreams(List<StreamConfiguration> streamsToUpdate) {

    for (StreamConfiguration streamConf : streamsToUpdate) {
      try {
        log.info("Performing stream update for {}", streamConf.getName());
        this.nc.jetStreamManagement().updateStream(streamConf);
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to update stream with info: " + streamConf, e);
      }
    }
  }

  public void deleteStreams(List<StreamConfiguration> streamsToDelete) {

    for (StreamConfiguration streamConf : streamsToDelete) {
      try {
        log.info("Performing stream deletion for {}", streamConf.getName());
        this.nc.jetStreamManagement().deleteStream(streamConf.getName());
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to delete stream with info: " + streamConf, e);
      }
    }
  }

  public void createKeyValues(List<KeyValueConfiguration> keyValuesToCreate) {

    for (KeyValueConfiguration keyValueConf : keyValuesToCreate) {
      try {
        log.info("Performing keyValue creation for {}", keyValueConf.getBucketName());
        this.nc.keyValueManagement().create(keyValueConf);
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to create keyValue with info: " + keyValueConf, e);
      }
    }
  }

  public void updateKeyValues(List<KeyValueConfiguration> keyValuesToUpdate) {

    for (KeyValueConfiguration keyValueConf : keyValuesToUpdate) {
      try {
        log.info("Performing keyValue update for {}", keyValueConf.getBucketName());
        this.nc.keyValueManagement().update(keyValueConf);
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to update keyValue with info: " + keyValueConf, e);
      }
    }
  }

  public void deleteKeyValues(List<KeyValueConfiguration> keyValuesToDelete) {

    for (KeyValueConfiguration keyValueConf : keyValuesToDelete) {
      try {
        log.info("Performing keyValue deletion for {}", keyValueConf.getBucketName());
        this.nc.keyValueManagement().delete(keyValueConf.getBucketName());
      } catch (IOException | JetStreamApiException e) {
        log.error("Unable to delete keyValue with info: " + keyValueConf, e);
      }
    }
  }

  public List<StreamInfo> getCurrentStreamConfig() throws IOException, JetStreamApiException {
    return this.nc.jetStreamManagement().getStreams()
        // filter key values
        .stream().filter(streamInfo -> !streamInfo.getConfiguration().getName().startsWith("KV_"))
        .collect(Collectors.toList());
  }

  public List<KeyValueConfiguration> getCurrentKeyValuesConfig() throws IOException, JetStreamApiException {
    return this.nc.keyValueManagement().getStatuses().stream()
        .map(KeyValueStatus::getConfiguration)
        .collect(Collectors.toList());
  }

  public Connection getConnection() {
    return this.nc;
  }
}
