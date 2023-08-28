package codes.nibby.callsign.viewer;

import codes.nibby.callsign.api.*;
import codes.nibby.callsign.api.sinks.CsvFileSink;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static void main(String[] args) throws InterruptedException {
//        generateMixedEvents(Paths.get(System.getProperty("user.dir")), "mixedEvents");
        generateIntervalEventPair().publishTo(new CsvFileSink(Paths.get(System.getProperty("user.home")).resolve("testDataFile")));
    }

    public static Result generateSingleInstantEvent() {
        var event = new InstantEvent("TestInstantEvent", System.nanoTime());

        generateRandomAttributes(event, 10);

        return new Result(event);
    }

    public static Result generateSingleIntervalStartEvent() {
        var event = new IntervalStartEvent("TestIntervalStartEvent", System.nanoTime());

        generateRandomAttributes(event, 20);

        return new Result(event);
    }

    public static Result generateSingleIntervalEndEvent() {
        var event = new IntervalStartEvent("TestIntervalStartEvent", System.nanoTime());

        generateRandomAttributes(event, 20);

        return new Result(event);
    }

    public static Result generateIntervalEventPair() {
        return generateIntervalEventPair(null);
    }

    public static Result generateIntervalEventPair(@Nullable Long durationNs) {
        long realDuration;

        if (durationNs == null) {
            realDuration = TimeUnit.MILLISECONDS.toNanos((int) (Math.random() * 4000) + 500);
        } else {
            realDuration = durationNs;
        }

        var startEvent = new IntervalStartEvent("TestIntervalEventPair", System.nanoTime() - realDuration);
        var endEvent = new IntervalEndEvent(startEvent.getId(), startEvent.getName(), System.nanoTime());

        generateRandomAttributes(startEvent, 15);
        generateRandomAttributes(endEvent, 15);

        return new Result(startEvent, endEvent);
    }

    private static void generateRandomAttributes(Event event, int maximumRandomAttributeCount) {
        int attributes = (int) (Math.random() * maximumRandomAttributeCount);
        for (int i = 0; i < attributes; i++) {
            event.putAttribute("Attribute" + i, "AttributeValue" + i);
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
