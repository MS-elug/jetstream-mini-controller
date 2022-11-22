package com.amadeus.nats.jetstream.admin.health;

import com.amadeus.nats.jetstream.admin.NatsConnection;
import io.nats.client.Connection;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class NatsConnectionHealthCheck implements HealthCheck {

  public static final String NATS_CONNECTION_STATUS = "Nats connection status";

  private static final String NATS_CONNECTION_CHECK_NAME = "Nats connection - readiness check";

  private final NatsConnection natsConnection;

  @Inject
  public NatsConnectionHealthCheck(NatsConnection natsConnection) {
    this.natsConnection = natsConnection;
  }

  @Override
  public HealthCheckResponse call() {
    HealthCheckResponseBuilder response = HealthCheckResponse.builder()
        .name(NATS_CONNECTION_CHECK_NAME);

    if (natsConnection.getConnection() == null) {
      return response.down().withData(NATS_CONNECTION_STATUS, "null").build();
    } else if (Connection.Status.CONNECTED == natsConnection.getConnection().getStatus()) {
      response.up();
    } else {
      response.down();
    }
    return response.withData(NATS_CONNECTION_STATUS, natsConnection.getConnection().getStatus().toString())
        .build();
  }
}
