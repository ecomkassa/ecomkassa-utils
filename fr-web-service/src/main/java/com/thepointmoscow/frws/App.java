package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thepointmoscow.frws.BackendCommand.BackendCommandType.REGISTER;

@SpringBootApplication
@ComponentScan(basePackages = "com.thepointmoscow.frws")
@Slf4j
public class App implements CommandLineRunner {

    @Autowired
    @Getter
    private BackendGateway backend;
    @Autowired
    @Getter
    private FiscalGateway fiscal;
    @Value("${task.delay:30}")
    @Getter
    private Long executionDelay;

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    private final ScheduledExecutorService executor;

    public App(@Autowired ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void run(String... args) throws Exception {
        executor.submit(new FetchTask(backend, fiscal, this::callback));
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Shutdown raised an error.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void callback(Runnable task, BackendCommand command) {
        if (command != null && REGISTER.equals(command.getCommand())) {
            executor.submit(task);
        } else {
            executor.schedule(task, getExecutionDelay(), TimeUnit.SECONDS);
        }
    }
}
