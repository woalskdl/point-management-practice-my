package me.jay.fcp.job.expire;

import me.jay.fcp.job.validator.TodayJobParameterValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpirePointJobConfiguration {

    @Bean
    public Job expirePointJob(
            JobBuilderFactory jobBuilderFactory,
            TodayJobParameterValidator validator,
            Step expirePointStep
    ) {
        return jobBuilderFactory.get("expirePointJob")
                .validator(validator)
                .incrementer(new RunIdIncrementer())    // run.id 가 계속해서 증가해서 job parameter가 중복되지 않게 해줌.
                .start(expirePointStep)
                .build();
    }
}
