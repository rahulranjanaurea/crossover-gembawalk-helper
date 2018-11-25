package com.crossover.report;

import com.crossover.report.exception.AnalyzerException;
import com.crossover.report.exception.ValidationException;
import com.crossover.report.exception.ValidationReason;
import com.crossover.report.manager.AnalyzeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class CrossoverApplication implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(CrossoverApplication.class);
    private final AnalyzeManager analyseManager;
    private final Environment environment;

    @Autowired
    public CrossoverApplication(AnalyzeManager analyseManager, Environment environment) {
        this.analyseManager = analyseManager;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(CrossoverApplication.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            validateInput();
            analyseManager.analyze();
        } catch (ValidationException e) {
            logger.error(e.getReason().name() + ":" + e.getReason().getReason(), e);
        } catch (AnalyzerException e) {
            logger.error(e.getReason().name() + ":" + e.getReason().getReason(), e);
        } catch (Exception e) {
            logger.error("Application Failed for some reason: " + e.getMessage(), e);
        }
    }

    private void validateInput() {
        System.out.println("username:"+environment.getProperty("username"));
        System.out.println("password:"+environment.getProperty("password"));
        if (StringUtils.isEmpty(environment.getProperty("username"))) {
            throw new ValidationException(ValidationReason.USERNAME_MISSING);
        }
        if (StringUtils.isEmpty(environment.getProperty("password"))) {
            throw new ValidationException(ValidationReason.PASSWORD_MISSING);
        }

    }
}
