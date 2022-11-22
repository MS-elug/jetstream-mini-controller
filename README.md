# Jetstream mini-controller

A micro-controller for [Nats](https://nats.io/) Jetstream items.

This project is an alternative to the JetStream operator nack: https://github.com/nats-io/nack, built as a microservice.

It can create, update and delete Jetstream streams or Key-Value buckets based on some configuration provided in a file.

It doesn't require any CRD and uses a configmap to propagate the configuration to the watched file when deployed on a
kubernetes cluster.

It can also work without kubernetes, by just deploying it and synchronizing the configuration file using
another mean.


Example usage:

```
mvn clean install # Build, execute the tests, create the docker image and the helm chart.
mvn clean deploy # All of the above, and docker image publication to the configured registry.
```

```
# Install helm chart
# Using released chart
helm repo add jetstream-mini-controller https://AmadeusITGroup.github.io/jetstream-mini-controller
helm install jetstream-mini-controller/jetstream-mini-controller -f anyValues.yaml

# With local Build
helm install jetstream-mini-controller target/helm/jetstream-mini-controller-1.0-SNAPSHOT.tgz -f values.yaml 
```

Example values:

```
# service account needed to read the configmap
serviceAccountName: admin-user
# config map content
streams:
  - streamName: stream1
    subjects:
      - subject1
      - subject2
    storage: memory
    replicas: 1
    retention: limits
    maxAge: 60    
  - streamName: stream2
    subjects:
      - subject3
      - subject4
      - subject5
    storage: file
    replicas: 1
    retention: limits
    maxAge: 10  
  - streamName: stream3
    subjects:
      - subject6
      - subject7
    storage: memory
    replicas: 1
    retention: limits
    maxAge: 5
keyValues:
  - name: bucket1
    storage: memory
    replicas: 1
    maxHistoryPerKey: 15
    timeToLiveInSeconds: 60
  - name: bucket2
    storage: file
    replicas: 1
    maxHistoryPerKey: 1
    timeToLiveInSeconds: 600 
  - name: bucket3
    storage: memory
    replicas: 1
    maxHistoryPerKey: 5
    timeToLiveInSeconds: 30     
```

## Streams configuration

| Option     | Description                                   | Example value  |
|------------|-----------------------------------------------|----------------|
| streamName | Name of the stream                            | myStream       |
| subjects   | List of subjects to be included in the stream | - subject1.>   |
| storage    | Peristency type                               | file or memory |
| replicas   | Replication factor                            | 3              |
| retention  | Retention policy                              | limits         |
| maxAge     | Maximum retention time, in seconds            | 600            |

## Key-Values configuration

| Option              | Description                              | Example value  |
|---------------------|------------------------------------------|----------------|
| name                | Name of the key value bucket             | muBucket       |
| storage             | Peristency type                          | file or memory |
| replicas            | Replication factor                       | 3              |
| timeToLiveInSeconds | Maximum retention time, in seconds       | 600            |
| maxHistoryPerKey    | History to be kept per key in the bucket | 3              |

## Application configuration
The configuration uses [standard quarkus one](https://quarkus.io/guides/config-reference).

| Configuration     | Description                                                                                                                          | Default value         |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------|-----------------------|
| nats.url          | Nats cluster URL(s)                                                                                                                  | nats://localhost:7656 |
| config.fileName   | Name of the file containing the stream configuration                                                                                 | stream-config.txt     |
| config.mount.path | Configuration file path                                                                                                              | /work/config          |
| scheduler         | Value to which the [scheduler will be configured](https://quarkus.io/guides/scheduler) to wake up to and check the jetstream config. | 30s                   |

## Other built-in features

As a standard quarkus application, this microservice includes:
* [Readiness/liveness probes](https://quarkus.io/guides/smallrye-health)
* [Prometheus metrics via micrometer](https://quarkus.io/guides/micrometer)
* [Splunk log exporter](https://quarkiverse.github.io/quarkiverse-docs/quarkus-logging-splunk/dev/index.html) (deactivated by default)