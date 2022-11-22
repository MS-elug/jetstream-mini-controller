package com.amadeus.nats.jetstream.admin.embeddedresource;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

@Getter
@Setter
@Slf4j
public class NatsUser implements InitializingBean, DisposableBean {

  private String natsUri;

  private Connection natsConnection;

  public NatsUser(String natsUri) {
    this.natsUri = natsUri;
  }

  public void start(String incomingChannel, String outGoingChannel, UnaryOperator<byte[]> function) {
    final Dispatcher dispatcher = this.natsConnection.createDispatcher(msg -> {
      log.info("Received message on channel " + incomingChannel);
      if (msg.getHeaders() != null) {
        msg.getHeaders().forEach((key, value) -> log.info("Header - Key: {}, value {}", key, value));
      }
      final Message response = NatsMessage.builder()
          .data(function.apply(msg.getData()))
          .headers(msg.getHeaders())
          .subject(outGoingChannel)
          .build();
      this.natsConnection.publish(response);
    });

    dispatcher.subscribe(incomingChannel);
  }

  public void startAndReplyTo(String incomingChannel, UnaryOperator<byte[]> function) {
    final Dispatcher dispatcher = this.natsConnection.createDispatcher(msg -> {
      log.info("Received message on channel " + incomingChannel);
      if (msg.getHeaders() != null) {
        msg.getHeaders().forEach((key, value) -> log.info("Header - Key: {}, value {}", key, value));
      }
      final Message response = NatsMessage.builder()
          .data(function.apply(msg.getData()))
          .headers(msg.getHeaders())
          .subject(msg.getReplyTo())
          .build();
      this.natsConnection.publish(response);
    });

    dispatcher.subscribe(incomingChannel);
  }

  public Subscription sendAndWaitForResponse(byte[] payload, String outGoingChannel, String incomingChannel,
      Headers headers) {
    final Subscription sub = this.natsConnection.subscribe(incomingChannel);

    final Message message = NatsMessage.builder()
        .data(payload)
        .subject(outGoingChannel)
        .headers(headers)
        .build();
    this.natsConnection.publish(message);

    return sub;
  }

  public Subscription sendAndWaitForResponse(byte[] payload, String outGoingChannel, String incomingChannel,
      String replyTo,
      Headers headers) {
    final Subscription sub = this.natsConnection.subscribe(incomingChannel);

    final Message message = NatsMessage.builder()
        .data(payload)
        .subject(outGoingChannel)
        .replyTo(replyTo)
        .headers(headers)
        .build();
    this.natsConnection.publish(message);

    return sub;
  }

  public CompletableFuture<Message> request(byte[] payload, String outGoingChannel, Headers headers) {
    final Message message = NatsMessage.builder()
        .data(payload)
        .subject(outGoingChannel)
        .headers(headers)
        .build();
    return this.natsConnection.request(message);
  }

  public Subscription subscribe(String incomingChannel) {
    return this.natsConnection.subscribe(incomingChannel);
  }

  @Override
  public void destroy() {
    try {
      if (this.natsConnection != null) {
        this.natsConnection.close();
      }
    } catch (final InterruptedException e) {
      log.error("Nats connection interrupted", e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      this.natsConnection = Nats.connect(this.natsUri);
    } catch (final IOException | InterruptedException e) {
      log.error("Nats connection interrupted", e);
    }
  }

}
