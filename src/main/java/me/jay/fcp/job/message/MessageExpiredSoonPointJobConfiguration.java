package me.jay.fcp.job.message;

import me.jay.fcp.job.validator.TodayJobParameterValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageExpiredSoonPointJobConfiguration {

    @Bean
    public Job messageExpiredSoonPointJob(
            JobBuilderFactory jobBuilderFactory,
            TodayJobParameterValidator validator,
            Step messageExpireSoonPointStep
    ) {
        return jobBuilderFactory
                .get("messageExpiredSoonPointJob")
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(messageExpireSoonPointStep)
                .build();
    }

}
