package codes.nibby.callsign.viewer;

import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.TimedEvent;
import codes.nibby.callsign.api.TimelineLogger;
import codes.nibby.callsign.api.sinks.CsvFileSink;

import java.nio.file.Paths;

public class TestDataGenerator {

    public static void main(String[] args) throws InterruptedException {
        TimelineLogger logger = new TimelineLogger(new CsvFileSink(Paths.get(System.getProperty("user.dir")), "testData"));

        var instantEvent = new InstantEvent("TestInstantEvent1");
        instantEvent.putAttribute("TestAttributeInstant", "abc");
        logger.recordEvent(instantEvent);

        TimedEvent timeEvent = logger.recordEventStart("MyTestEvent");
        timeEvent.putAttribute("TimedAttribute", "def");
        timeEvent.putAttribute("TimedAttribute2", "Abc");

        Thread.sleep(1000);

        logger.recordEvent(new InstantEvent("TestInstantEvent2 - After Sleep"));
        logger.recordEventEnd(timeEvent);
    }

}
