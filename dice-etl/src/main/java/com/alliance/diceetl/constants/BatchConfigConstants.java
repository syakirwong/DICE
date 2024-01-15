package com.alliance.diceetl.constants;

public class BatchConfigConstants {

    public static final int CHUNK_SIZE = Integer.parseInt(System.getenv("batch.chunk.size"));
    public static final int CONCURRENCY_LIMIT = Integer.parseInt(System.getenv("batch.concurrency.limit"));
    public static final int PAGE_SIZE = Integer.parseInt(System.getenv("batch.page.size"));

    public static class InternetBankingActivation {
        public static final String SELECT_CLAUSE =System.getenv("internet.banking.query.select.clause");
        public static final String FROM_CLAUSE =System.getenv("internet.banking.query.from.clause");
        public static final String WHERE_CLAUSE =System.getenv("internet.banking.query.where.clause")==null?"":System.getenv("internet.banking.query.where.clause");
        public static final String SORT_KEY =System.getenv("internet.banking.query.sortkey.clause");

        private InternetBankingActivation() {}
    }

    public static class OnBoarding {
        public static final String SELECT_CLAUSE =System.getenv("vcc.onboarding.forms.query.select.clause");
        public static final String FROM_CLAUSE =System.getenv("vcc.onboarding.forms.query.from.clause");
        public static final String WHERE_CLAUSE =System.getenv("vcc.onboarding.forms.query.where.clause")==null?"":System.getenv("vcc.onboarding.forms.query.where.clause");
        public static final String SORT_KEY =System.getenv("vcc.onboarding.forms.query.sortkey.clause");

        private OnBoarding() {}
    }

    public static class PloanApplication {
        public static final String SELECT_CLAUSE =System.getenv("ploan.application.query.select.clause");
        public static final String FROM_CLAUSE =System.getenv("ploan.application.query.from.clause");
        public static final String WHERE_CLAUSE =System.getenv("ploan.application.query.where.clause")==null?"":System.getenv("ploan.application.query.where.clause");
        public static final String SORT_KEY =System.getenv("ploan.application.query.sortkey.clause");

        private PloanApplication() {}
    }

    public static class DdmSoleCC {
        public static final String SELECT_CLAUSE =System.getenv("ddm.solecc.query.select.clause");
        public static final String FROM_CLAUSE =System.getenv("ddm.solecc.query.from.clause");
        public static final String WHERE_CLAUSE =System.getenv("ddm.solecc.query.where.clause")==null?"":System.getenv("ddm.solecc.query.where.clause");
        public static final String SORT_KEY =System.getenv("ddm.solecc.query.sortkey.clause");

        private DdmSoleCC() {}
    }


    private BatchConfigConstants() {}
}
