package codes.nibby.callsign.viewer;

import codes.nibby.callsign.api.InstantEvent;
import codes.nibby.callsign.api.IntervalStartEvent;
import codes.nibby.callsign.api.TimelineLogger;
import codes.nibby.callsign.api.sinks.CsvFileSink;

import java.nio.file.Paths;

public class TestDataGenerator {

    public static void main(String[] args) throws InterruptedException {
        TimelineLogger logger = new TimelineLogger(new CsvFileSink(Paths.get(System.getProperty("user.dir")), "testData"));

//        var instantEvent = new InstantEvent("TestInstantEvent1");
//        instantEvent.putAttribute("TestAttributeInstant", "abc");
//        instantEvent.putAttribute("threadId", "0");
//        logger.recordEvent(instantEvent);

        IntervalStartEvent timeEvent = logger.recordEventStart("MyTestEvent");
        timeEvent.putAttribute("threadId", "0");
        timeEvent.putAttribute("TimedAttribute", "def");
        timeEvent.putAttribute("TimedAttribute2", "Abc");

        Thread.sleep(1000);

        logger.recordEvent(new InstantEvent("TestInstantEvent2 - After Sleep"));
        logger.recordEventEnd(timeEvent);

        IntervalStartEvent timeEvent2 = logger.recordEventStart("MyTestEvent2");
        timeEvent2.putAttribute("threadId", "1");
        Thread.sleep(2000);
        logger.recordEventEnd(timeEvent2);
        Thread.sleep(2000);

        var instantEvent2 = new InstantEvent("TestInstantEvent2");
        instantEvent2.putAttribute("TestAttributeInstant", "def");
        instantEvent2.putAttribute("threadId", "0");
        logger.recordEvent(instantEvent2);
    }

}
