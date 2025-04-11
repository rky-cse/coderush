package me.rkycse.coderush.config;

import me.rkycse.coderush.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataMigrationRunner implements ApplicationRunner {

    private final UserService userService;

    public DataMigrationRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // You can add a conditional so it only runs once
        System.out.println("Running RankEntity startTime migration...");
        int updated = userService.migrateStartTimeToRankEntities();
        System.out.println("Migration complete: " + updated + " records updated.");
    }
}