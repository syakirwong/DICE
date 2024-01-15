package com.alliance.diceetl.constants;

import java.util.Map;

public class BatchConstants {

    public static class JobNames {
        public static final String INTERNET_BANKING_ACTIVATION = "InternetBankingActivationJob";
        public static final String DDM_SOLE_CC = "DdmSoleCCJob";
        public static final String ONBOARDING_FORMS = "OnBoardingFormsBatchJob";
        public static final String PLOAN_APPLICATION = "PloanApplicationJob";

        private JobNames() {}
    }

    public static class StepNames {
        public static final String INTERNET_BANKING_ACTIVATION = "InternetBankingActivationStep";
        public static final String DDM_SOLE_CC = "DdmSoleCCStep";
        public static final String ONBOARDING_FORMS = "OnBoardingFormsStep";
        public static final String PLOAN_APPLICATION = "PloanApplicationStep";

        private StepNames() {}
    }

    private static final Map<String, String> jobNameToSqliteTriggerMap = Map.of(
            JobNames.ONBOARDING_FORMS, "vcconboardingforms",
            JobNames.DDM_SOLE_CC, "solecc",
            JobNames.INTERNET_BANKING_ACTIVATION, "internetbanking",
            JobNames.PLOAN_APPLICATION, "ploanapplication"
    );

    public static String getSqliteTriggerMap(String jobName){
        return jobNameToSqliteTriggerMap.get(jobName);
    }



    public enum OperationType {
        READ,WRITE,SKIP
    }


    private BatchConstants() {}
}

