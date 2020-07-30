package nl.jads.sodalite.scheduler;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitoringDataCollector {
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private TimerTask repeatedTask = new MonitoringTimerTask();

    public void start()
            throws InterruptedException {
        long delay = 1000L;
        long period = 1000L;
        System.out.println("Monitoring Data Collector Started");
        executor.scheduleAtFixedRate(repeatedTask, delay, period, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        System.out.println("Monitoring Data Collector Stopped");
        executor.shutdown();
    }
}


