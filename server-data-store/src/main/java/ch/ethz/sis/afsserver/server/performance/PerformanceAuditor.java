package ch.ethz.sis.afsserver.server.performance;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PerformanceAuditor {

    private List<Pair<Event, Long>> eventsWithTimes;
    private long start;

    public PerformanceAuditor() {
        eventsWithTimes = new ArrayList<>();
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void audit(@NonNull Event event) {
        long now = System.currentTimeMillis();

        eventsWithTimes.add(new Pair<>(event, (now - start)));
        start = now;
    }

    @Override
    public String toString() {
        long total = 0;
        for (Pair<Event, Long> eventWithTime:eventsWithTimes) {
            total += eventWithTime.getValue();
        }
        return "Total (From Events) = " + total + " => " + eventsWithTimes;
    }
}
