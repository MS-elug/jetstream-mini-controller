package com.amadeus.nats.jetstream.admin;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.api.StreamConfiguration;

class NatsConnectionTest {

  private NatsConnection nc;

  private JetStreamManagement jsm;

  private static MockedStatic<Nats> mockedNats;

  @BeforeAll
  public static void beforeAll() {
    mockedNats = mockStatic(Nats.class);
  }

  @BeforeEach
  public void setUp() throws IOException {
    Connection natsCo = mock(Connection.class);
    mockedNats.when(() -> Nats.connect(any(Options.class))).thenReturn(natsCo);
    this.jsm = mock(JetStreamManagement.class);
    when(natsCo.jetStreamManagement()).thenReturn(this.jsm);

    try {
      this.nc = new NatsConnection("");
    } catch (IOException | InterruptedException e) {
      fail(e);
    }
  }

  @Test
  void testStreamCreationWithSomeItems() throws IOException, JetStreamApiException {
    List<StreamConfiguration> streams = createStreams();
    this.nc.createStreams(streams);

    verify(this.jsm, times(2)).addStream(any(StreamConfiguration.class));
  }

  @Test
  void testStreamCreationWithNoItems() throws IOException, JetStreamApiException {
    this.nc.createStreams(new ArrayList<>());

    verify(this.jsm, never()).addStream(any(StreamConfiguration.class));
  }

  @Test
  void testStreamUpdateWithSomeItems() throws IOException, JetStreamApiException {
    List<StreamConfiguration> streams = createStreams();
    this.nc.updateStreams(streams);

    verify(this.jsm, times(2)).updateStream(any(StreamConfiguration.class));
  }

  @Test
  void testStreamUpdateWithNoItems() throws IOException, JetStreamApiException {
    this.nc.updateStreams(new ArrayList<>());

    verify(this.jsm, never()).updateStream(any(StreamConfiguration.class));
  }

  @Test
  void testStreamDeletionWithSomeItems() throws IOException, JetStreamApiException {
    List<StreamConfiguration> streams = createStreams();
    this.nc.deleteStreams(streams);

    verify(this.jsm, times(2)).deleteStream(any(String.class));
  }

  @Test
  void testStreamDeletionWithNoItems() throws IOException, JetStreamApiException {
    this.nc.deleteStreams(new ArrayList<>());

    verify(this.jsm, never()).deleteStream(any(String.class));
  }

  private List<StreamConfiguration> createStreams() {
    List<StreamConfiguration> streams = new ArrayList<>();

    streams.add(StreamConfiguration.builder().name("name1").build());
    streams.add(StreamConfiguration.builder().name("name2").build());

    return streams;
  }

}
