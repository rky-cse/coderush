package me.rkycse.coderush;

import me.rkycse.coderush.service.MTMTournamentSchedulerService;
import me.rkycse.coderush.service.RankListSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoderushApplication {
	@Autowired
	private MTMTournamentSchedulerService schedulerService;
	@Autowired
	private RankListSchedulerService rankListSchedulerService;

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(CoderushApplication.class, args);
		MTMTournamentSchedulerService schedulerService = context.getBean(MTMTournamentSchedulerService.class);
		schedulerService.startScheduling();
		RankListSchedulerService rankListSchedulerService = context.getBean(RankListSchedulerService.class);
		rankListSchedulerService.startScheduling();

	}

}
