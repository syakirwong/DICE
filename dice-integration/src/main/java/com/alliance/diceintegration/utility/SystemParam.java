package com.alliance.diceintegration.utility;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class SystemParam {

    private final Properties properties = new Properties();
    private static SystemParam systemParam = null;

    private String dataSourceDriverClassName;
    private String dbobdbUrl;
    private String dbobdbUsername;
    private String dbobdbPassword;
    private String ib2gUrl;
    private String ib2gUsername;
    private String ib2gPassword;
    private List<String> mailFailureHandleAlertTo;
    private String localDataCenter;
    private String entityBasePackage;
    private String hosts;
    private Integer port;
    private String keyspace;
    private String username;
    private String password;

    @Autowired
    public SystemParam(@Value("${jdbc.ClassName}") String dataSourceDriverClassName,
            @Value("${jdbc.DBOSDB}") String dbobdbUrl,
            @Value("${jdbc.DBOSDB.username}") String dbobdbUsername,
            @Value("${jdbc.DBOSDB.password}") String dbobdbPassword,
            @Value("${jdbc.IB2GDB}") String ib2gUrl,
            @Value("${jdbc.IB2GDB.username}") String ib2gUsername,
            @Value("${jdbc.IB2GDB.password}") String ib2gPassword,
            @Value("#{'${spring.mail.failure.handle.alert.to}'.split(',')}") List<String> mailFailureHandleAlertTo,
            @Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
            @Value("${spring.data.cassandra.entity-base-package}") String entityBasePackage,
            @Value("${spring.data.cassandra.contact-points}") String hosts,
            @Value("${spring.data.cassandra.port}") Integer port,
            @Value("${spring.data.cassandra.keyspace-name}") String keyspace,
            @Value("${spring.data.cassandra.username}") String username,
            @Value("${spring.data.cassandra.password}") String password)
            {
        if (systemParam == null) {
            systemParam = new SystemParam();
            systemParam.init();
        }
        // this.ACTIVEMQ_BOCKER_IP = aCTIVEMQ_BOCKER_IP;
        SystemParam.getInstance().setDataSourceDriverClassName(dataSourceDriverClassName);
        SystemParam.getInstance().setDbobdbUrl(dbobdbUrl);
        SystemParam.getInstance().setDbobdbUsername(dbobdbUsername);
        SystemParam.getInstance().setDbobdbPassword(dbobdbPassword);
        SystemParam.getInstance().setIb2gUrl(ib2gUrl);
        SystemParam.getInstance().setIb2gUsername(ib2gUsername);
        SystemParam.getInstance().setIb2gPassword(ib2gPassword);
        SystemParam.getInstance().setMailFailureHandleAlertTo(mailFailureHandleAlertTo);
        SystemParam.getInstance().setLocalDataCenter(localDataCenter);
        SystemParam.getInstance().setEntityBasePackage(entityBasePackage);
        SystemParam.getInstance().setHosts(hosts);
        SystemParam.getInstance().setPort(port);
        SystemParam.getInstance().setKeyspace(keyspace);
        SystemParam.getInstance().setUsername(username);
        SystemParam.getInstance().setPassword(password);

    }

    public SystemParam() {
    }

    public static SystemParam getInstance() {
        if (systemParam == null) {
            systemParam = new SystemParam();
            systemParam.init();
        }
        return systemParam;
    }

    public void init() {
        try {
            ClassLoader classLoader = SystemParam.class.getClassLoader();
            InputStream applicationPropertiesStream = classLoader.getResourceAsStream("application.properties");
            properties.load(applicationPropertiesStream);

        } catch (Exception e) {

        }
    }

    public String getDB2className() {
        return (SystemParam.getInstance().getDataSourceDriverClassName() != null
                && SystemParam.getInstance().getDataSourceDriverClassName().compareTo("") != 0)
                        ? SystemParam.getInstance().getDataSourceDriverClassName()
                        : properties.getProperty("jdbc.ClassName");
    }

    public String getDBOSConnectionString() {
        return (SystemParam.getInstance().getDbobdbUrl() != null
                && SystemParam.getInstance().getDbobdbUrl().compareTo("") != 0)
                        ? SystemParam.getInstance().getDbobdbUrl()
                        : properties.getProperty("jdbc.DBOSDB");
    }

    public String getDBOSUsername() {
        return (SystemParam.getInstance().getDbobdbUsername() != null
                && SystemParam.getInstance().getDbobdbUsername().compareTo("") != 0)
                        ? SystemParam.getInstance().getDbobdbUsername()
                        : properties.getProperty("jdbc.DBOSDB.username");
    }

    public String getDBOSPassword() {
        return (SystemParam.getInstance().getDbobdbPassword() != null
                && SystemParam.getInstance().getDbobdbPassword().compareTo("") != 0)
                        ? SystemParam.getInstance().getDbobdbPassword()
                        : properties.getProperty("jdbc.DBOSDB.password");
    }

    public String getIB2GConnectionString() {
        return (SystemParam.getInstance().getIb2gUrl() != null
                && SystemParam.getInstance().getIb2gUrl().compareTo("") != 0) ? SystemParam.getInstance().getIb2gUrl()
                        : properties.getProperty("jdbc.IB2GDB");
    }

    public String getIB2GUsername() {
        return (SystemParam.getInstance().getIb2gUsername() != null
                && SystemParam.getInstance().getIb2gUsername().compareTo("") != 0)
                        ? SystemParam.getInstance().getIb2gUsername()
                        : properties.getProperty("jdbc.IB2GDB.username");
    }

    public String getIB2GPassword() {
        return (SystemParam.getInstance().getIb2gPassword() != null
                && SystemParam.getInstance().getIb2gPassword().compareTo("") != 0)
                        ? SystemParam.getInstance().getIb2gPassword()
                        : properties.getProperty("jdbc.IB2GDB.password");
    }

    public String getDataSourceDriverClassName() {
        return dataSourceDriverClassName;
    }

    public void setDataSourceDriverClassName(String dataSourceDriverClassName) {
        this.dataSourceDriverClassName = dataSourceDriverClassName;
    }

    public String getDbobdbUrl() {
        return dbobdbUrl;
    }

    public void setDbobdbUrl(String dbobdbUrl) {
        this.dbobdbUrl = dbobdbUrl;
    }

    public String getDbobdbUsername() {
        return dbobdbUsername;
    }

    public void setDbobdbUsername(String dbobdbUsername) {
        this.dbobdbUsername = dbobdbUsername;
    }

    public String getDbobdbPassword() {
        return dbobdbPassword;
    }

    public void setDbobdbPassword(String dbobdbPassword) {
        this.dbobdbPassword = dbobdbPassword;
    }

    public String getIb2gUrl() {
        return ib2gUrl;
    }

    public void setIb2gUrl(String ib2gUrl) {
        this.ib2gUrl = ib2gUrl;
    }

    public String getIb2gUsername() {
        return ib2gUsername;
    }

    public void setIb2gUsername(String ib2gUsername) {
        this.ib2gUsername = ib2gUsername;
    }

    public String getIb2gPassword() {
        return ib2gPassword;
    }

    public void setIb2gPassword(String ib2gPassword) {
        this.ib2gPassword = ib2gPassword;
    }

    public List<String> getMailFailureHandleAlertTo() {
        return this.mailFailureHandleAlertTo;
    }

    public void setMailFailureHandleAlertTo(List<String> mailFailureHandleAlertTo) {
        this.mailFailureHandleAlertTo = mailFailureHandleAlertTo;
    }

}
