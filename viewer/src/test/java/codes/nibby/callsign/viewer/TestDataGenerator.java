package codes.nibby.callsign.viewer;

import codes.nibby.callsign.api.*;
import codes.nibby.callsign.api.sinks.CsvFileSink;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static void main(String[] args) {
        Result result = generateIntervalEventPair();

        for (int i = 0; i < 20; i++) {
            Event event = generateSingleInstantEvent().asList().get(0);
            event.putAttribute("index", String.valueOf(i));

            result = result.combine(new Result(event));

            int intervals = (int) (Math.random() * 10);
            for (int ii = 0; ii < intervals; ii++) {
                Result result1 = generateIntervalEventPair(TimeUnit.SECONDS.toMillis((int) (Math.random() * 5)));
                result1.generatedEvents.get(0).putAttribute("index", String.valueOf(i));
                result1.generatedEvents.get(1).putAttribute("index", String.valueOf(i));
                result = result.combine(result1);
                try {
                    Thread.sleep((int) (Math.random() * 1000) + 10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        result.publishTo(new CsvFileSink(Paths.get(System.getProperty("user.dir")).resolve("testDataGeneratorData2")));
    }

    public static Result generateSingleInstantEvent() {
        var event = new InstantEvent("TestInstantEvent", Instant.now().toEpochMilli());

        generateDeterministicAttributes(event);

        return new Result(event);
    }

    public static Result generateSingleIntervalStartEvent() {
        var event = new IntervalStartEvent("TestIntervalStartEvent", Instant.now().toEpochMilli());

        generateDeterministicAttributes(event);

        return new Result(event);
    }

    public static Result generateSingleIntervalEndEvent() {
        var event = new IntervalEndEvent(UUID.randomUUID(), "TestIntervalEndEvent", Instant.now().toEpochMilli());

        generateDeterministicAttributes(event);

        return new Result(event);
    }

    public static Result generateIntervalEventPair() {
        return generateIntervalEventPair(null);
    }

    public static Result generateIntervalEventPair(@Nullable Long durationMs) {
        long realDurationMs;

        if (durationMs == null) {
            realDurationMs = (int) (Math.random() * 4000) + 500;
        } else {
            realDurationMs = durationMs;
        }

        var startEvent = new IntervalStartEvent("TestIntervalEventPair", Instant.now().toEpochMilli() - realDurationMs);
        var endEvent = new IntervalEndEvent(startEvent.getId(), startEvent.getName(), Instant.now().toEpochMilli());

        generateDeterministicAttributes(startEvent);
        generateDeterministicAttributes(endEvent);

        return new Result(startEvent, endEvent);
    }

    private static void generateRandomAttributes(Event event, int maximumRandomAttributeCount) {
        int attributes = (int) (Math.random() * maximumRandomAttributeCount);
        for (int i = 0; i < attributes; i++) {
            event.putAttribute("Attribute" + i, "AttributeValue" + i);
        }
    }

    private static void generateDeterministicAttributes(Event event) {
        for (int i = 0; i < 10; i++) {
            event.putAttribute("TestAttribute" + i, "Value" + i);
        }
    }

    public static class Result {

        private final List<Event> generatedEvents;

        private Result(Event ... generatedEvents) {
            this.generatedEvents = Arrays.stream(generatedEvents).collect(Collectors.toList());
        }

        private Result(List<Event> generatedEvents) {
            this.generatedEvents = generatedEvents;
        }

        public List<Event> asList() {
            return Collections.unmodifiableList(generatedEvents);
        }

        public void publishTo(TimelineLogSink sink) {
            generatedEvents.forEach(sink::publishEvent);
        }

        public Result combine(Result ... others) {
            List<Event> allEvents = new ArrayList<>(generatedEvents);

            for (Result other : others) {
                allEvents.addAll(other.generatedEvents);
            }

            return new Result(allEvents);
        }
    }

}
