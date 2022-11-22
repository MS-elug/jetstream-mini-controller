package com.amadeus.nats.jetstream.admin;

import com.amadeus.nats.jetstream.admin.model.JetstreamConfigs;
import com.amadeus.nats.jetstream.admin.model.KeyValueConfig;
import com.amadeus.nats.jetstream.admin.model.StreamConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class JetstreamConfigResolverTest {

  private static final String VALID_CONF_WITH_STREAMS = "ValidConfWithStreamsOnly.yaml";

  private static final String VALID_CONF_WITH_KEY_VALUES = "ValidConfWithKeyValuesOnly.yaml";

  private static final String VALID_CONF_WITH_STREAMS_AND_KEY_VALUES = "ValidConfWithStreamsAndKeyValues.yaml";

  private static final String INVALID_CONF = "InvalidConf.yaml";

  private static final String RESOURCES_BASE_PATH = "src/test/resources/com/amadeus/nats/jetstream/admin";

  @Test
  void testSuccessFulConfParsingWithStreamsOnly() {

    JetstreamConfigResolver resolver = new JetstreamConfigResolver(RESOURCES_BASE_PATH, VALID_CONF_WITH_STREAMS);

    JetstreamConfigs configs = resolver.resolveConfig();

    assertNotNull(configs);
    assertEquals(0, configs.getKeyValueConfigs().size());
    assertEquals(3, configs.getStreamConfigs().size());

    checkStreams(configs);
  }

  @Test
  void testSuccessFulConfParsingWithKeyValuesOnly() {

    JetstreamConfigResolver resolver = new JetstreamConfigResolver(RESOURCES_BASE_PATH, VALID_CONF_WITH_KEY_VALUES);

    JetstreamConfigs configs = resolver.resolveConfig();

    assertNotNull(configs);
    assertEquals(3, configs.getKeyValueConfigs().size());
    assertEquals(0, configs.getStreamConfigs().size());

    checkKeyValues(configs);
  }

  @Test
  void testSuccessFulConfParsingWithStreamsAndKeyValues() {

    JetstreamConfigResolver resolver = new JetstreamConfigResolver(RESOURCES_BASE_PATH, VALID_CONF_WITH_STREAMS_AND_KEY_VALUES);

    JetstreamConfigs configs = resolver.resolveConfig();

    assertNotNull(configs);
    assertEquals(3, configs.getKeyValueConfigs().size());
    assertEquals(3, configs.getStreamConfigs().size());

    checkStreams(configs);
    checkKeyValues(configs);
  }

  @Test
  void testErrorInConfParsing() {

    JetstreamConfigResolver resolver = new JetstreamConfigResolver(RESOURCES_BASE_PATH, INVALID_CONF);
    assertNull(resolver.resolveConfig());
  }


  private static void checkStreams(JetstreamConfigs configs) {
    for (StreamConfig config: configs.getStreamConfigs()) {
      if ("stream1".equals(config.getStreamName())) {

        assertEquals(60, config.getMaxAge());
        assertEquals(3, config.getReplicas());
        assertEquals("memory", config.getStorage());
        assertEquals(Arrays.asList("subject1", "subject2"), config.getSubjects());
        assertEquals("limits", config.getRetention());


      } else if ("stream2".equals(config.getStreamName())) {

        assertEquals(10, config.getMaxAge());
        assertEquals(1, config.getReplicas());
        assertEquals("file", config.getStorage());
        assertEquals(Arrays.asList("subject3", "subject4", "subject5"), config.getSubjects());
        assertEquals("limits", config.getRetention());

      } else if ("stream3".equals(config.getStreamName())) {

        assertEquals(5, config.getMaxAge());
        assertEquals(2, config.getReplicas());
        assertEquals("memory", config.getStorage());
        assertEquals(Arrays.asList("subject6", "subject7"), config.getSubjects());
        assertEquals("limits", config.getRetention());

      } else {
        fail("should not have happened");
      }
    }
  }

  private static void checkKeyValues(JetstreamConfigs configs) {
    for (KeyValueConfig config: configs.getKeyValueConfigs()) {
      if ("bucket1".equals(config.getName())) {

        assertEquals(60, config.getTimeToLiveInSeconds());
        assertEquals(3, config.getReplicas());
        assertEquals("memory", config.getStorage());
        assertEquals(15, config.getMaxHistoryPerKey());


      } else if ("bucket2".equals(config.getName())) {

        assertEquals(600, config.getTimeToLiveInSeconds());
        assertEquals(2, config.getReplicas());
        assertEquals("file", config.getStorage());
        assertEquals(1, config.getMaxHistoryPerKey());

      } else if ("bucket3".equals(config.getName())) {

        assertEquals(30, config.getTimeToLiveInSeconds());
        assertEquals(1, config.getReplicas());
        assertEquals("memory", config.getStorage());
        assertEquals(5, config.getMaxHistoryPerKey());

      } else {
        fail("should not have happened");
      }
    }
  }
}
