package com.alliance.diceetl.config.springbatchlistener;

import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.model.BatchTrackingModel;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class StepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        StepExecutionListener.super.beforeStep(stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        BatchTrackingModel.getInstance().addDataCountByStepNameAndOperation(stepExecution.getStepName(), BatchConstants.OperationType.READ, stepExecution.getReadCount());
        BatchTrackingModel.getInstance().addDataCountByStepNameAndOperation(stepExecution.getStepName(), BatchConstants.OperationType.SKIP, stepExecution.getSkipCount());
        BatchTrackingModel.getInstance().addDataCountByStepNameAndOperation(stepExecution.getStepName(), BatchConstants.OperationType.WRITE, stepExecution.getWriteCount());

        return StepExecutionListener.super.afterStep(stepExecution);
    }
}
