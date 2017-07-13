package ru.unlocker.mongodbmigrator;

import com.mongodb.Mongo;
import lombok.extern.slf4j.Slf4j;
import org.mongeez.MongeezRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@SpringBootApplication
@Slf4j
public class MongodbMigratorApplication {

    /**
     * Creates and tunes runner.
     *
     * @param mongo MongoDB connection
     * @param props properties
     * @return runner
     */
    @Bean("mongeez")
    public MongeezRunner mongeezRunner(Mongo mongo, MongoProperties props) {
        MongeezRunner runner = new MongeezRunner();
        runner.setMongo(mongo);
        runner.setDbName(props.getDatabase());
        runner.setExecuteEnabled(true);
        runner.setFile(new ClassPathResource("db/mongeez.xml"));
        return runner;
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(MongodbMigratorApplication.class, args)) {
            MongeezRunner runner = ctx.getBean(MongeezRunner.class);
            runner.execute();
        }
    }
}
