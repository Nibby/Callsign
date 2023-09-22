package codes.nibby.callsign.viewer.ui.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class TimelineDescriptors {

    private static final DateFormat TIMELINE_DATE_FORMAT_DD_MM_YY = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat TIMELINE_TIME_FORMAT_HH_MM = new SimpleDateFormat("HH:mm");
    private static final DateFormat TIMELINE_TIME_FORMAT_HH_MM_SS = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI = new SimpleDateFormat("HH:mm:ss.SSS");

    static final List<TraceViewViewport.TimelineDescriptor> PREFERRED_VALUES = new ArrayList<>();
    static final TraceViewViewport.TimelineDescriptor LAST_RESORT;

    static {
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(30, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(14, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(7, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(6, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(5, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(4, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(3, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(2, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(1, TimeUnit.DAYS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));

        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(12, TimeUnit.HOURS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(6, TimeUnit.HOURS, TIMELINE_DATE_FORMAT_DD_MM_YY, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(5, TimeUnit.HOURS, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(4, TimeUnit.HOURS, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(3, TimeUnit.HOURS, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(2, TimeUnit.HOURS, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(1, TimeUnit.HOURS, null, TIMELINE_TIME_FORMAT_HH_MM));

        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(30, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(20, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(10, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(5, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(2, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(1, TimeUnit.MINUTES, null, TIMELINE_TIME_FORMAT_HH_MM_SS));

        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(30, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(20, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(10, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(5, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(2, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(1, TimeUnit.SECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS));

        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(500, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(200, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(100, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(50, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(20, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(10, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(5, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(2, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TraceViewViewport.TimelineDescriptor(1, TimeUnit.MILLISECONDS, null, TIMELINE_TIME_FORMAT_HH_MM_SS_MILLI));

        LAST_RESORT = PREFERRED_VALUES.get(PREFERRED_VALUES.size() - 1);
    }

    private TimelineDescriptors() {
        // Helper class, no instantiation
    }

}
