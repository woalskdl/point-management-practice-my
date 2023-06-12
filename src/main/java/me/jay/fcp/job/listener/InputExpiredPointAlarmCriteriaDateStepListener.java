package me.jay.fcp.job.listener;

import org.springframework.batch.core.*;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class InputExpiredPointAlarmCriteriaDateStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        // today jobParameter 를 가져온다.
        // today - 1 한 뒤, alarmCriteriaDate 라는 이름으로 StepExecutionContext 에 넣는다.
        JobParameter todayParameter = stepExecution.getJobParameters().getParameters().get("today");
        if (todayParameter == null)
            return;

        LocalDate today = LocalDate.parse((String) todayParameter.getValue());
        ExecutionContext context = stepExecution.getExecutionContext();
        context.put("alarmCriteriaDate", today.minusDays(1).format(DateTimeFormatter.ISO_DATE));
        stepExecution.setExecutionContext(context);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
