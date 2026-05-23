package me.rkycse.coderush.problemgeneratoragent.tools.execution;

import me.rkycse.coderush.problemgeneratoragent.dto.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sprint 1 stub: always returns compile success.
 * Sprint 2 will implement real compilation via the judge.
 *
 * The real implementation requires a scratch question with test files
 * set up in the filesystem, which is part of the Sprint 2 execution
 * tool wiring.
 */
@Component
public class CompileCodeTool {

    private static final Logger logger = LoggerFactory.getLogger(CompileCodeTool.class);

    public CompileResult compile(String code, String language) {
        // TODO Sprint 2: send to judge via Kafka and wait for response
        logger.debug("CompileCodeTool (stub): skipping compile check for {} code", language);
        return new CompileResult(true, null);
    }
}
