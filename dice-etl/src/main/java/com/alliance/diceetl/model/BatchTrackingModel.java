package com.alliance.diceetl.model;

import com.alliance.diceetl.constants.BatchConstants;

import java.util.concurrent.atomic.AtomicLong;


public class BatchTrackingModel {
    private BatchTrackingModel() {}

    private static BatchTrackingModel batchTrackingModelInstance = null;
    public static BatchTrackingModel getInstance() {
        if (batchTrackingModelInstance == null) {
            batchTrackingModelInstance = new BatchTrackingModel();
        }
        return batchTrackingModelInstance;
    }

    private AtomicLong onBoardingWriteDataCount = new AtomicLong(0L);
    private AtomicLong onBoardingReadDataCount = new AtomicLong(0L);
    private AtomicLong onBoardingSkipDataCount = new AtomicLong(0L);

    private AtomicLong ddmSoleCcWriteDataCount = new AtomicLong(0L);
    private AtomicLong ddmSoleCcReadDataCount = new AtomicLong(0L);
    private AtomicLong ddmSoleCcSkipDataCount = new AtomicLong(0L);

    private AtomicLong internetBankingActivationWriteDataCount = new AtomicLong(0L);
    private AtomicLong internetBankingActivationReadDataCount = new AtomicLong(0L);
    private AtomicLong internetBankingActivationSkipDataCount = new AtomicLong(0L);

    private AtomicLong ploanApplicationWriteDataCount = new AtomicLong(0L);
    private AtomicLong ploanApplicationReadDataCount = new AtomicLong(0L);
    private AtomicLong ploanApplicationSkipDataCount = new AtomicLong(0L);

    public AtomicLong getDataCountByBatchJobAndOperation(String jobNames,BatchConstants.OperationType operationType){
        return switch (jobNames){
            case BatchConstants.JobNames.DDM_SOLE_CC -> getDdmSoleCCDataCount(operationType);
            case BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION -> getInternetBankingActivationDataCount(operationType);
            case BatchConstants.JobNames.ONBOARDING_FORMS -> getOnBoardingDataCount(operationType);
            case BatchConstants.JobNames.PLOAN_APPLICATION -> getPloanApplicationDataCount(operationType);
            default -> throw new IllegalStateException("Unexpected value: " + jobNames);
        };
    }

    public void addDataCountByStepNameAndOperation(String stepName,BatchConstants.OperationType operationType, Long countToAdd){
        switch (stepName){
            case BatchConstants.StepNames.DDM_SOLE_CC -> addDdmSoleCCDataCount(countToAdd,operationType);
            case BatchConstants.StepNames.INTERNET_BANKING_ACTIVATION -> addInternetBankingActivationDataCount(countToAdd,operationType);
            case BatchConstants.StepNames.ONBOARDING_FORMS -> addOnBoardingData(countToAdd,operationType);
            case BatchConstants.StepNames.PLOAN_APPLICATION -> addPloanApplicationDataCount(countToAdd,operationType);
        }
    }

    public void resetCountsByJobName(String jobName){
        switch (jobName){
            case BatchConstants.JobNames.DDM_SOLE_CC -> resetDdmSoleCCDataCount();
            case BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION -> resetInternetBankingActivationDataCount();
            case BatchConstants.JobNames.ONBOARDING_FORMS -> resetOnBoardingCount();
            case BatchConstants.JobNames.PLOAN_APPLICATION -> resetPloanApplicationDataCount();
        }
    }


    private AtomicLong getOnBoardingDataCount(BatchConstants.OperationType operationType) {
        return switch (operationType) {
            case WRITE -> onBoardingWriteDataCount;
            case READ -> onBoardingReadDataCount;
            case SKIP -> onBoardingSkipDataCount;
        };
    }

    private void addOnBoardingData(Long countToAdd, BatchConstants.OperationType operationType) {
        switch (operationType) {
            case WRITE -> onBoardingWriteDataCount.addAndGet(countToAdd);
            case READ -> onBoardingReadDataCount.addAndGet(countToAdd);
            case SKIP -> onBoardingSkipDataCount.addAndGet(countToAdd);
        }
    }


    private AtomicLong getDdmSoleCCDataCount(BatchConstants.OperationType operationType) {
        return switch (operationType) {
            case WRITE -> ddmSoleCcWriteDataCount;
            case READ -> ddmSoleCcReadDataCount;
            case SKIP -> ddmSoleCcSkipDataCount;
        };
    }

    private void addDdmSoleCCDataCount(Long ddmSoleCCDataCount,BatchConstants.OperationType operationType) {
        switch (operationType) {
            case WRITE -> ddmSoleCcWriteDataCount.addAndGet(ddmSoleCCDataCount);
            case READ -> ddmSoleCcReadDataCount.addAndGet(ddmSoleCCDataCount);
            case SKIP -> ddmSoleCcSkipDataCount.addAndGet(ddmSoleCCDataCount);
        }
    }

    private AtomicLong getInternetBankingActivationDataCount(BatchConstants.OperationType operationType) {
        return switch (operationType) {
            case WRITE -> internetBankingActivationWriteDataCount;
            case READ -> internetBankingActivationReadDataCount;
            case SKIP -> internetBankingActivationSkipDataCount;
        };
    }

    private void addInternetBankingActivationDataCount(Long internetBankingActivationDataCount,BatchConstants.OperationType operationType) {
        switch (operationType) {
            case WRITE -> internetBankingActivationWriteDataCount.addAndGet(internetBankingActivationDataCount);
            case READ -> internetBankingActivationReadDataCount.addAndGet(internetBankingActivationDataCount);
            case SKIP -> internetBankingActivationSkipDataCount.addAndGet(internetBankingActivationDataCount);
        };
    }

    private AtomicLong getPloanApplicationDataCount(BatchConstants.OperationType operationType) {
        return switch (operationType) {
            case WRITE -> ploanApplicationWriteDataCount;
            case READ -> ploanApplicationReadDataCount;
            case SKIP -> ploanApplicationSkipDataCount;
        };
    }

    private void addPloanApplicationDataCount(Long ploanApplicationDataCount, BatchConstants.OperationType operationType) {
        switch (operationType) {
            case WRITE -> ploanApplicationWriteDataCount.addAndGet(ploanApplicationDataCount);
            case READ -> ploanApplicationReadDataCount.addAndGet(ploanApplicationDataCount);
            case SKIP -> ploanApplicationSkipDataCount.addAndGet(ploanApplicationDataCount);
        };
    }

    private void resetOnBoardingCount() {
        onBoardingSkipDataCount = new AtomicLong(0L);
        onBoardingReadDataCount = new AtomicLong(0L);
        onBoardingWriteDataCount = new AtomicLong(0L);
    }

    private void resetPloanApplicationDataCount() {
        ploanApplicationSkipDataCount = new AtomicLong(0L);
        ploanApplicationReadDataCount = new AtomicLong(0L);
        ploanApplicationWriteDataCount = new AtomicLong(0L);
    }

    private void resetInternetBankingActivationDataCount() {
        internetBankingActivationSkipDataCount = new AtomicLong(0L);
        internetBankingActivationReadDataCount = new AtomicLong(0L);
        internetBankingActivationWriteDataCount = new AtomicLong(0L);
    }

    private void resetDdmSoleCCDataCount() {
        ddmSoleCcSkipDataCount = new AtomicLong(0L);
        ddmSoleCcReadDataCount = new AtomicLong(0L);
        ddmSoleCcWriteDataCount = new AtomicLong(0L);
    }

}

