package nl.jads.sodalite.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimerTask;

public class MonitoringTimerTask  extends TimerTask {
    @Override
    public void run() {
//        System.out.println("Data Collected at: "
//                + LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()),
//                ZoneId.systemDefault()));
        try {
            Thread.sleep(100 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
