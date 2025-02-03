package me.rkycse.coderush;

import jakarta.annotation.PostConstruct;
import me.rkycse.coderush.entity.TestcaseEntity;
import me.rkycse.coderush.repository.TestcaseRepository;
import me.rkycse.coderush.service.RankListSchedulerService;
import me.rkycse.coderush.service.TournamentSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
public class CoderushApplication {
	@Autowired
	private TournamentSchedulerService schedulerService;
	private RankListSchedulerService rankListSchedulerService;

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(CoderushApplication.class, args);
		TournamentSchedulerService schedulerService = context.getBean(TournamentSchedulerService.class);
		schedulerService.startScheduling();
		RankListSchedulerService rankListSchedulerService = context.getBean(RankListSchedulerService.class);
		rankListSchedulerService.startScheduling();
	}

}
