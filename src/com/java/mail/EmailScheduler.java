package com.java.mail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailScheduler {
	public static void scheduleEmail(Runnable task, LocalDateTime sendTime) {
        long delay = Duration.between(LocalDateTime.now(), sendTime).toMillis();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }
}