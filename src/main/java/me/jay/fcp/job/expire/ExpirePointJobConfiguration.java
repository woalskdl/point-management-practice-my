package me.jay.fcp.job.expire;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpirePointJobConfiguration {

    @Bean
    public Job expirePointJob(
            JobBuilderFactory jobBuilderFactory,
            Step expirePointStep
    ) {
        return jobBuilderFactory.get("expirePointJob")
                .start(expirePointStep)
                .build();
    }
}
