package codes.nibby.callsign.viewer;

import codes.nibby.callsign.api.*;
import codes.nibby.callsign.api.sinks.CsvFileSink;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
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

        generateMixedEvents(Paths.get(System.getProperty("user.dir")), "mixedEvents");
    }

    public static Result generateSingleInstantEvent() {
        var event = new InstantEvent("TestInstantEvent", System.nanoTime());

        generateRandomAttributes(event, 10);

        return new Result(event);
    }

    private static void generateRandomAttributes(Event event, int maximumRandomAttributeCount) {
        int attributes = (int) (Math.random() * maximumRandomAttributeCount);
        for (int i = 0; i < attributes; i++) {
            event.putAttribute("Attribute" + i, "AttributeValue" + i);
        }
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

        generateRandomAttributes(endEvent, 15);

        return new Result(startEvent, endEvent);
    }

    public static void generateMixedEvents(Path outputFolder, String nameWithoutExtension) throws InterruptedException {
        TimelineLogger logger = new TimelineLogger(new CsvFileSink(outputFolder, nameWithoutExtension));

        var instantEvent = new InstantEvent("TestInstantEvent1", System.nanoTime());
        instantEvent.putAttribute("TestAttributeInstant", "abc");
        instantEvent.putAttribute("threadId", "0");
        logger.recordEvent(instantEvent);

        IntervalStartEvent timeEvent = logger.recordEventStart("MyTestEvent");
        timeEvent.putAttribute("threadId", "0");
        timeEvent.putAttribute("TimedAttribute", "def");
        timeEvent.putAttribute("TimedAttribute2", "Abc");

        Thread.sleep(1000);

        logger.recordEvent(new InstantEvent("TestInstantEvent2 - After Sleep", System.nanoTime()));
        logger.recordEventEnd(timeEvent);

        IntervalStartEvent timeEvent2 = logger.recordEventStart("MyTestEvent2");
        timeEvent2.putAttribute("threadId", "1");
        Thread.sleep(2000);
        logger.recordEventEnd(timeEvent2);
        Thread.sleep(2000);

        var instantEvent2 = new InstantEvent("TestInstantEvent2", System.nanoTime());
        instantEvent2.putAttribute("TestAttributeInstant", "def");
        instantEvent2.putAttribute("threadId", "0");
        logger.recordEvent(instantEvent2);
    }

    public static class Result {

        private final List<Event> generatedEvents;

        private Result(Event ... generatedEvents) {
            this.generatedEvents = Arrays.stream(generatedEvents).collect(Collectors.toList());
        }

        private Result(List<Event> generatedEvents) {
            this.generatedEvents = generatedEvents;
        }

        public List<Event> getGeneratedEvents() {
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
