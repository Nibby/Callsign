package codes.nibby.callsign.viewer.ui.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static codes.nibby.callsign.viewer.ui.view.TraceViewViewport.*;

final class TimelineDescriptors {

    private static final DateFormat DATE_FORMAT_DD_MM_YY = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat TIME_FORMAT_HH_MM = new SimpleDateFormat("HH:mm");
    private static final DateFormat TIME_FORMAT_HH_MM_SS = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat TIME_FORMAT_HH_MM_SS_MILLI = new SimpleDateFormat("HH:mm:ss.SSS");

    static final List<TimelineDescriptor> PREFERRED_VALUES = new ArrayList<>();
    static final TimelineDescriptor LAST_RESORT;

    static {
        PREFERRED_VALUES.add(new TimelineDescriptor(30, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(14, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(7, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(6, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(5, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(4, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(3, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(2, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(1, TimeUnit.DAYS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));

        PREFERRED_VALUES.add(new TimelineDescriptor(12, TimeUnit.HOURS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(6, TimeUnit.HOURS, DATE_FORMAT_DD_MM_YY, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(5, TimeUnit.HOURS, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(4, TimeUnit.HOURS, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(3, TimeUnit.HOURS, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(2, TimeUnit.HOURS, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(1, TimeUnit.HOURS, null, TIME_FORMAT_HH_MM));

        PREFERRED_VALUES.add(new TimelineDescriptor(30, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(20, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(10, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM));
        PREFERRED_VALUES.add(new TimelineDescriptor(5, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(2, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(1, TimeUnit.MINUTES, null, TIME_FORMAT_HH_MM_SS));

        PREFERRED_VALUES.add(new TimelineDescriptor(30, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(20, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(10, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(5, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(2, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));
        PREFERRED_VALUES.add(new TimelineDescriptor(1, TimeUnit.SECONDS, null, TIME_FORMAT_HH_MM_SS));

        PREFERRED_VALUES.add(new TimelineDescriptor(500, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(200, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(100, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(50, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(20, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(10, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(5, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(2, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));
        PREFERRED_VALUES.add(new TimelineDescriptor(1, TimeUnit.MILLISECONDS, null, TIME_FORMAT_HH_MM_SS_MILLI));

        LAST_RESORT = PREFERRED_VALUES.get(PREFERRED_VALUES.size() - 1);
    }

    private TimelineDescriptors() {
        // Helper class, no instantiation
    }

}
