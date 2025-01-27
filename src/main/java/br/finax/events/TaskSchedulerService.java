package br.finax.events;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TaskSchedulerService {
    private final ThreadPoolTaskScheduler taskScheduler;

    public TaskSchedulerService() {
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(2);
        this.taskScheduler.initialize();
    }

    public void scheduleTask(Runnable task, Instant instant) {
        this.taskScheduler.schedule(task, instant);
    }
}
