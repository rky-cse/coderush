package me.rkycse.coderush.problemgeneratoragent.job;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentJobRepository extends CrudRepository<AgentJob, String> {}
