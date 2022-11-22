package com.amadeus.nats.jetstream.admin;

import com.amadeus.nats.jetstream.admin.comparators.KeyValueConfigComparator;
import com.amadeus.nats.jetstream.admin.comparators.StreamConfigComparator;
import com.amadeus.nats.jetstream.admin.model.JetstreamConfigs;
import com.amadeus.nats.jetstream.admin.model.KeyValueConfig;
import com.amadeus.nats.jetstream.admin.model.StreamConfig;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
public class JetstreamUpdatesScheduler {

  private final NatsConnection nc;

  private final JetstreamConfigResolver streamConf;

  @Inject
  public JetstreamUpdatesScheduler(NatsConnection nc, JetstreamConfigResolver streamConf) {
    this.nc = nc;
    this.streamConf = streamConf;
  }

  @Scheduled(every = "${scheduler}")
  void updateStreams() {

    log.debug("Waking up to check the jetstream configuration");

    JetstreamConfigs jetstreamConfigs = this.streamConf.resolveConfig();

    log.debug("Streams to be configured: {}", jetstreamConfigs);

    if (jetstreamConfigs != null) {

      manageStreams(jetstreamConfigs.getStreamConfigs());
      manageKeyValues(jetstreamConfigs.getKeyValueConfigs());

    } else {
      log.debug("No stream configured");
    }
  }

  private void manageKeyValues(List<KeyValueConfig> keyValueConfigs) {
    try {
      List<KeyValueConfiguration> currentConfigs = this.nc.getCurrentKeyValuesConfig();

      log.debug("Current KeyValues: " + currentConfigs);

      List<KeyValueConfiguration> keyValuesToCreate = KeyValueConfigComparator.getKVToCreate(currentConfigs, keyValueConfigs);

      this.nc.createKeyValues(keyValuesToCreate);

      List<KeyValueConfiguration> keyValuesToUpdate = KeyValueConfigComparator.compareKVWithConfig(currentConfigs,
          keyValueConfigs);

      this.nc.updateKeyValues(keyValuesToUpdate);

      List<KeyValueConfiguration> keyValuesToDelete = KeyValueConfigComparator.getKVToDelete(currentConfigs,
          keyValueConfigs);

      this.nc.deleteKeyValues(keyValuesToDelete);

      log.debug("KeyValues created {}, keyValues updated {}, keyValues deleted {}", keyValuesToCreate.size(),
          keyValuesToUpdate.size(), keyValuesToDelete.size());

    } catch (IOException | JetStreamApiException e) {
      log.error("Error when using jetstream api", e);
    }
  }

  private void manageStreams(List<StreamConfig> streamConfigs) {
    try {
      List<StreamInfo> currentConfigs = this.nc.getCurrentStreamConfig();

      log.debug("Current streams: " + currentConfigs);

      List<StreamConfiguration> streamsToCreate = StreamConfigComparator.getStreamsToCreate(currentConfigs, streamConfigs);

      this.nc.createStreams(streamsToCreate);

      List<StreamConfiguration> streamsToUpdate = StreamConfigComparator.compareStreamsWithConfig(currentConfigs,
          streamConfigs);

      this.nc.updateStreams(streamsToUpdate);

      List<StreamConfiguration> streamsToDelete = StreamConfigComparator.getStreamsToDelete(currentConfigs,
          streamConfigs);

      this.nc.deleteStreams(streamsToDelete);

      log.debug("Streams created {}, streams updated {}, streams deleted {}", streamsToCreate.size(),
          streamsToUpdate.size(), streamsToDelete.size());

    } catch (IOException | JetStreamApiException e) {
      log.error("Error when using jetstream api", e);
    }
  }
}
