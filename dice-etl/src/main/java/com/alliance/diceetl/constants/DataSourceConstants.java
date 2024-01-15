package com.alliance.diceetl.constants;

public class DataSourceConstants {

    public static class VCC {
        public static final String DATASOURCE_URL = System.getenv("vcc.datasource.url");
        public static final String SCHEMA = System.getenv("vcc.schema");
        public static final String USERNAME = System.getenv("vcc.username");
        public static final String PASSWORD = System.getenv("vcc.password");

        private VCC() {}
    }

    public static class AOP {
        public static final String DATASOURCE_URL = System.getenv("aop.datasource.url");
        public static final String SCHEMA = System.getenv("aop.schema");
        public static final String USERNAME = System.getenv("aop.username");
        public static final String PASSWORD = System.getenv("aop.password");

        private AOP() {}
    }

    public static class DBOBDB {
        public static final String DATASOURCE_URL = System.getenv("dbobdb.datasource.url");
        public static final String SCHEMA = System.getenv("dbobdb.schema");
        public static final String DESTINATION_SCHEMA = System.getenv("dbobdb.destination.schema");
        public static final String USERNAME = System.getenv("dbobdb.username");
        public static final String PASSWORD = System.getenv("dbobdb.password");

        private DBOBDB() {}
    }

    public static class PLDB {
        public static final String DATASOURCE_URL = System.getenv("pldb.datasource.url");
        public static final String SCHEMA = System.getenv("pldb.schema");
        public static final String USERNAME = System.getenv("pldb.username");
        public static final String PASSWORD = System.getenv("pldb.password");

        private PLDB() {}
    }


}
