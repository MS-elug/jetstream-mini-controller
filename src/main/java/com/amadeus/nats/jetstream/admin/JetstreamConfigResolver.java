package com.amadeus.nats.jetstream.admin;

import com.amadeus.nats.jetstream.admin.model.JetstreamConfigs;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class JetstreamConfigResolver {

  private String configPath;

  private ObjectMapper mapper;

  @Inject
  public JetstreamConfigResolver(@ConfigProperty(name = "config.mount.path") String jetStreamConfigPath,
      @ConfigProperty(name = "config.fileName") String fileName) {
    this.mapper = new ObjectMapper(new YAMLFactory());
    this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.findAndRegisterModules();

    this.configPath = jetStreamConfigPath + "/" + fileName;
  }

  public JetstreamConfigs resolveConfig() {
    JetstreamConfigs jetstreamConfigs = null;
    try {
      Path filePath = Paths.get(this.configPath);
      String config = new String(Base64.getDecoder().decode(Files.readAllLines(filePath).stream()
          .collect(Collectors.joining(""))), StandardCharsets.UTF_8);

      log.debug("Jetstream config read again");

      jetstreamConfigs = this.mapper.readValue(config, JetstreamConfigs.class);

      if (CollectionUtils.isEmpty(jetstreamConfigs.getStreamConfigs())) {
        jetstreamConfigs.setStreamConfigs(new ArrayList<>());
      }

    } catch (IOException | IllegalArgumentException e) {
      log.error("Unable to parse the config provided", e);
    }
    return jetstreamConfigs;
  }

}
