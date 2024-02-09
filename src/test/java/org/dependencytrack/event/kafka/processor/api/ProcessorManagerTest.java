package org.dependencytrack.event.kafka.processor.api;

import alpine.Config;
import net.mguenther.kafka.junit.ExternalKafkaCluster;
import net.mguenther.kafka.junit.KeyValue;
import net.mguenther.kafka.junit.SendKeyValues;
import net.mguenther.kafka.junit.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.dependencytrack.common.ConfigKey;
import org.dependencytrack.event.kafka.KafkaTopics.Topic;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ProcessorManagerTest {

    @Rule
    public RedpandaContainer kafkaContainer = new RedpandaContainer(DockerImageName
            .parse("docker.redpanda.com/vectorized/redpanda:v23.3.3"));

    private ExternalKafkaCluster kafka;
    private Config configMock;

    @Before
    public void setUp() {
        kafka = ExternalKafkaCluster.at(kafkaContainer.getBootstrapServers());

        configMock = mock(Config.class);
        when(configMock.getProperty(eq(ConfigKey.KAFKA_BOOTSTRAP_SERVERS)))
                .thenReturn(kafkaContainer.getBootstrapServers());
    }

    @Test
    public void testSingleRecordProcessor() throws Exception {
        final var inputTopic = new Topic<>("input", Serdes.String(), Serdes.String());
        kafka.createTopic(TopicConfig.withName(inputTopic.name()).withNumberOfPartitions(3));

        final var recordsProcessed = new AtomicInteger(0);

        when(configMock.getPassThroughProperties(eq("kafka.processor.foo.consumer")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.processing.order", "key",
                        "kafka.processor.foo.max.concurrency", "5",
                        "kafka.processor.foo.consumer.auto.offset.reset", "earliest"
                ));

        final Processor<String, String> processor =
                record -> recordsProcessed.incrementAndGet();

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerProcessor("foo", inputTopic, processor);

            for (int i = 0; i < 100; i++) {
                kafka.send(SendKeyValues.to("input", List.of(new KeyValue<>("foo" + i, "bar" + i)))
                        .with(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                        .with(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
            }

            processorManager.startAll();

            await("Record Processing")
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(recordsProcessed).hasValue(100));
        }
    }

    @Test
    public void testSingleRecordProcessorRetry() throws Exception {
        final var inputTopic = new Topic<>("input", Serdes.String(), Serdes.String());
        kafka.createTopic(TopicConfig.withName(inputTopic.name()).withNumberOfPartitions(3));

        final var attemptsCounter = new AtomicInteger(0);

        final var objectSpy = spy(new Object());
        when(objectSpy.toString())
                .thenThrow(new RuntimeException(new TimeoutException()))
                .thenThrow(new RuntimeException(new TimeoutException()))
                .thenThrow(new RuntimeException(new TimeoutException()))
                .thenReturn("done");

        final Processor<String, String> processor = record -> {
            attemptsCounter.incrementAndGet();
            var ignored = objectSpy.toString();
        };

        when(configMock.getPassThroughProperties(eq("kafka.processor.foo")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.retry.initial.delay.ms", "5",
                        "kafka.processor.foo.retry.multiplier", "1",
                        "kafka.processor.foo.retry.max.delay.ms", "10"
                ));
        when(configMock.getPassThroughProperties(eq("kafka.processor.foo.consumer")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.consumer.auto.offset.reset", "earliest"
                ));

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerProcessor("foo", inputTopic, processor);

            kafka.send(SendKeyValues.to("input", List.of(new KeyValue<>("foo", "bar")))
                    .with(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                    .with(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));

            processorManager.startAll();

            await("Record Processing")
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(attemptsCounter).hasValue(4));
        }
    }

    @Test
    public void testBatchProcessor() throws Exception {
        final var inputTopic = new Topic<>("input", Serdes.String(), Serdes.String());
        kafka.createTopic(TopicConfig.withName(inputTopic.name()).withNumberOfPartitions(3));

        final var recordsProcessed = new AtomicInteger(0);
        final var actualBatchSizes = new ConcurrentLinkedQueue<>();

        when(configMock.getPassThroughProperties(eq("kafka.processor.foo")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.processing.order", "key",
                        "kafka.processor.foo.max.batch.size", "100"
                ));
        when(configMock.getPassThroughProperties(eq("kafka.processor.foo.consumer")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.consumer.auto.offset.reset", "earliest"
                ));

        final BatchProcessor<String, String> recordProcessor = records -> {
            recordsProcessed.addAndGet(records.size());
            actualBatchSizes.add(records.size());
        };

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerBatchProcessor("foo", inputTopic, recordProcessor);

            for (int i = 0; i < 1_000; i++) {
                kafka.send(SendKeyValues.to("input", List.of(new KeyValue<>("foo" + i, "bar" + i)))
                        .with(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                        .with(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
            }

            processorManager.startAll();

            await("Record Processing")
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(recordsProcessed).hasValue(1_000));

            assertThat(actualBatchSizes).containsOnly(100);
        }
    }

    @Test
    public void testWithMaxConcurrencyMatchingPartitionCount() throws Exception {
        final var inputTopic = new Topic<>("input", Serdes.String(), Serdes.String());

        kafka.createTopic(TopicConfig.withName(inputTopic.name()).withNumberOfPartitions(12));

        when(configMock.getPassThroughProperties(eq("kafka.processor.foo")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.processing.order", "partition",
                        "kafka.processor.foo.max.concurrency", "-1"
                ));
        when(configMock.getPassThroughProperties(eq("kafka.processor.foo.consumer")))
                .thenReturn(Map.of(
                        "kafka.processor.foo.consumer.auto.offset.reset", "earliest"
                ));

        final var threadNames = new ArrayBlockingQueue<String>(100);
        final Processor<String, String> processor = record -> {
            threadNames.add(Thread.currentThread().getName());
        };

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerProcessor("foo", inputTopic, processor);

            for (int i = 0; i < 100; i++) {
                kafka.send(SendKeyValues.to("input", List.of(new KeyValue<>("foo" + i, "bar" + i)))
                        .with(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
                        .with(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
            }

            processorManager.startAll();

            await("Record Processing")
                    .atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(threadNames).hasSize(100));

            assertThat(threadNames.stream().distinct()).hasSize(12);
        }
    }

    @Test
    public void testStartAllWithMissingTopics() {
        final var inputTopicA = new Topic<>("input-a", Serdes.String(), Serdes.String());
        final var inputTopicB = new Topic<>("input-b", Serdes.String(), Serdes.String());

        kafka.createTopic(TopicConfig.withName(inputTopicA.name()).withNumberOfPartitions(3));

        final Processor<String, String> processor = record -> {
        };

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerProcessor("a", inputTopicA, processor);
            processorManager.registerProcessor("b", inputTopicB, processor);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(processorManager::startAll)
                    .withMessage("""
                            Existence of 1 topic(s) could not be verified: \
                            [{topic=input-b, error=org.apache.kafka.common.errors.UnknownTopicOrPartitionException: \
                            This server does not host this topic-partition.}]""");
        }
    }

    @Test
    public void testProbeHealth() {
        final var inputTopic = new Topic<>("input", Serdes.String(), Serdes.String());
        kafka.createTopic(TopicConfig.withName(inputTopic.name()).withNumberOfPartitions(3));

        final Processor<String, String> processor = record -> {
        };

        try (final var processorManager = new ProcessorManager(UUID.randomUUID(), configMock)) {
            processorManager.registerProcessor("foo", inputTopic, processor);

            {
                final HealthCheckResponse healthCheckResponse = processorManager.probeHealth();
                assertThat(healthCheckResponse.getName()).isEqualTo("kafka-processors");
                assertThat(healthCheckResponse.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
                assertThat(healthCheckResponse.getData()).isPresent();
                assertThatJson(healthCheckResponse.getData().get())
                        .isEqualTo("""
                                {
                                  "foo": "UP"
                                }
                                """);
            }

            processorManager.startAll();

            {
                final HealthCheckResponse healthCheckResponse = processorManager.probeHealth();
                assertThat(healthCheckResponse.getName()).isEqualTo("kafka-processors");
                assertThat(healthCheckResponse.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
                assertThat(healthCheckResponse.getData()).isPresent();
                assertThatJson(healthCheckResponse.getData().get())
                        .isEqualTo("""
                                {
                                  "foo": "UP"
                                }
                                """);
            }

            processorManager.close();

            {
                final HealthCheckResponse healthCheckResponse = processorManager.probeHealth();
                assertThat(healthCheckResponse.getName()).isEqualTo("kafka-processors");
                assertThat(healthCheckResponse.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
                assertThat(healthCheckResponse.getData()).isPresent();
                assertThatJson(healthCheckResponse.getData().get())
                        .isEqualTo("""
                                {
                                  "foo": "DOWN"
                                }
                                """);
            }
        }
    }

}