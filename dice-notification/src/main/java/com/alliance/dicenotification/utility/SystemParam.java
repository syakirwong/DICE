package com.alliance.dicenotification.utility;

import java.io.InputStream;
import java.util.Properties;

import com.alliance.dicenotification.constant.SFTPProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class SystemParam {

    private final Properties properties = new Properties();
    private static SystemParam systemParam = null;

    private String activemqBockerIp;
    private String activemqUsername;
    private String activemqPassword;
    // private String activemqConsumer;
    private String activemqTopic;
    private String pooledConnectionFactoryConfig;
    private String maxConnection;
    private String maxThreadPoolSize;
    private String minConnection;

    private String localDataCenter;
    private String entityBasePackage;
    private String hosts;
    private Integer port;
    private String keyspace;
    private String username;
    private String password;

    //Migrated var
    private String sftpUser;
    private String sftpHost;
    private String sftpPort;
    private String sftpPassword;
    private String sftpPath;
    private String sftpUser2;
    private String sftpHost2;
    private String sftpPort2;
    private String sftpPassword2;
    private String sftpPath2;
    private String environment;
    private String mock;

    @Autowired
    public SystemParam(@Value("${activemq.service.ip}") String activemqBockerIp,
            @Value("${activemq.username}") String activemqUsername,
            @Value("${activemq.password}") String activemqPassword,
            // @Value("${activemq.consumer}") String activemqConsumer,
            @Value("${activemq.pooledConnectionFactory}") String pooledConnectionFactoryConfig,
            @Value("${activemq.maxConnection}") String maxConnection,
            @Value("${activemq.MaxThreadPoolSize}") String maxThreadPoolSize,
            @Value("${activemq.minConnection}") String minConnection,
            @Value("${activemq.topic}") String activemqTopic,
            @Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
            @Value("${spring.data.cassandra.entity-base-package}") String entityBasePackage,
            @Value("${spring.data.cassandra.contact-points}") String hosts,
            @Value("${spring.data.cassandra.port}") Integer port,
            @Value("${spring.data.cassandra.keyspace-name}") String keyspace,
            @Value("${spring.data.cassandra.username}") String username,
            @Value("${spring.data.cassandra.password}") String password,

                       @Value("${sftp.worker01.user}") String sftpUser,
                       @Value("${sftp.worker01.host}") String sftpHost,
                       @Value("${sftp.worker01.port}") String sftpPort,
                       @Value("${sftp.worker01.path}") String sftpPath,
                       @Value("${sftp.worker01.password}") String sftpPassword,

                       @Value("${sftp.worker02.user}") String sftpUser2,
                       @Value("${sftp.worker02.host}") String sftpHost2,
                       @Value("${sftp.worker02.port}") String sftpPort2,
                       @Value("${sftp.worker02.path}") String sftpPath2,
                       @Value("${sftp.worker02.password}") String sftpPassword2,

                       @Value("${sftp.environment}") String sftpEnvironment,
                       @Value("${sftp.mock}") String sftpMock            ) {
        if (systemParam == null) {
            systemParam = new SystemParam();
            systemParam.init();
        }
        // this.ACTIVEMQ_BOCKER_IP = aCTIVEMQ_BOCKER_IP;
        SystemParam.getInstance().setActivemqBockerIp(activemqBockerIp);
        SystemParam.getInstance().setActivemqUsername(activemqUsername);
        SystemParam.getInstance().setActivemqPassword(activemqPassword);
        // SystemParam.getInstance().setActivemqConsumer(activemqConsumer);
        SystemParam.getInstance().setActivemqTopic(activemqTopic);
        SystemParam.getInstance().setPooledConnectionFactoryConfig(pooledConnectionFactoryConfig);
        SystemParam.getInstance().setMaxConnection(maxConnection);
        SystemParam.getInstance().setMaxThreadPoolSize(maxThreadPoolSize);
        SystemParam.getInstance().setMinConnection(minConnection);
        SystemParam.getInstance().setLocalDataCenter(localDataCenter);
        SystemParam.getInstance().setEntityBasePackage(entityBasePackage);
        SystemParam.getInstance().setHosts(hosts);
        SystemParam.getInstance().setPort(port);
        SystemParam.getInstance().setKeyspace(keyspace);
        SystemParam.getInstance().setUsername(username);
        SystemParam.getInstance().setPassword(password);

        //Migrate
        SystemParam.getInstance().setSftpUser(sftpUser);
        SystemParam.getInstance().setSftpHost(sftpHost);
        SystemParam.getInstance().setSftpPort(sftpPort);
        SystemParam.getInstance().setSftpPath(sftpPath);
        SystemParam.getInstance().setSftpPassword(sftpPassword);

        SystemParam.getInstance().setSftpUser2(sftpUser2);
        SystemParam.getInstance().setSftpHost2(sftpHost2);
        SystemParam.getInstance().setSftpPort2(sftpPort2);
        SystemParam.getInstance().setSftpPath2(sftpPath2);
        SystemParam.getInstance().setSftpPassword2(sftpPassword2);

        SystemParam.getInstance().setEnvironment(sftpEnvironment);
        SystemParam.getInstance().setMock(sftpMock);

        // System.out.print(SystemParam.getInstance().getACTIVEMQ_BOCKER_IP());
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

    public String getBrokerUrl() {
        return (SystemParam.getInstance().getActivemqBockerIp() != null
                && SystemParam.getInstance().getActivemqBockerIp().compareTo("") != 0)
                        ? SystemParam.getInstance().getActivemqBockerIp()
                        : properties.getProperty("activemq.service.ip");
    }

    public String getPooledConnectionFactory() {
        return (SystemParam.getInstance().getPooledConnectionFactoryConfig() != null
                && SystemParam.getInstance().getPooledConnectionFactoryConfig().compareTo("") != 0)
                        ? SystemParam.getInstance().getPooledConnectionFactoryConfig()
                        : properties.getProperty("activemq.pooledConnectionFactory");

    }

    public String getAmqUsername() {
        return (SystemParam.getInstance().getActivemqUsername() != null
                && SystemParam.getInstance().getActivemqUsername().compareTo("") != 0)
                        ? SystemParam.getInstance().getActivemqUsername()
                        : properties.getProperty("activemq.username");
    }

    public String getAmqPassword() {
        return (SystemParam.getInstance().getActivemqPassword() != null
                && SystemParam.getInstance().getActivemqPassword().compareTo("") != 0)
                        ? SystemParam.getInstance().getActivemqPassword()
                        : properties.getProperty("activemq.password");
    }

    // public String getConsumer() {
    // return (SystemParam.getInstance().getActivemqConsumer() != null
    // && SystemParam.getInstance().getActivemqConsumer().compareTo("") != 0)
    // ? SystemParam.getInstance().getActivemqConsumer()
    // : properties.getProperty("activemq.consumer");
    // }

    public String getTopic() {
        return (SystemParam.getInstance().getActivemqTopic() != null
                && SystemParam.getInstance().getActivemqTopic().compareTo("") != 0)
                        ? SystemParam.getInstance().getActivemqTopic()
                        : properties.getProperty("activemq.topic");
    }

    public String getActivemqBockerIp() {
        return activemqBockerIp;
    }

    public void setActivemqBockerIp(String activemqBockerIp) {
        this.activemqBockerIp = activemqBockerIp;
    }

    public String getActivemqUsername() {
        return activemqUsername;
    }

    public void setActivemqUsername(String activemqUsername) {
        this.activemqUsername = activemqUsername;
    }

    public String getActivemqPassword() {
        return activemqPassword;
    }

    public void setActivemqPassword(String activemqPassword) {
        this.activemqPassword = activemqPassword;
    }

    // public String getActivemqConsumer() {
    // return activemqConsumer;
    // }

    // public void setActivemqConsumer(String activemqConsumer) {
    // this.activemqConsumer = activemqConsumer;
    // }

    public String getActivemqTopic() {
        return activemqTopic;
    }

    public void setActivemqTopic(String activemqTopic) {
        this.activemqTopic = activemqTopic;
    }

    public String getPooledConnectionFactoryConfig() {
        return pooledConnectionFactoryConfig;
    }

    public void setPooledConnectionFactoryConfig(String pooledConnectionFactoryConfig) {
        this.pooledConnectionFactoryConfig = pooledConnectionFactoryConfig;
    }

    public String getMaxConnection() {
        return (maxConnection != null && maxConnection.compareTo("") != 0) ? maxConnection
                : properties.getProperty("activemq.maxConnection");
    }

    public void setMaxConnection(String maxConnection) {
        this.maxConnection = maxConnection;
    }

    public String getMaxThreadPoolSize() {
        return (maxThreadPoolSize != null && maxThreadPoolSize.compareTo("") != 0) ? maxThreadPoolSize
                : properties.getProperty("activemq.MaxThreadPoolSize");
    }

    public void setMaxThreadPoolSize(String maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    public String getMinConnection() {
        return (minConnection != null && minConnection.compareTo("") != 0) ? minConnection
                : properties.getProperty("activemq.minConnection");
    }

    public void setMinConnection(String minConnection) {
        this.minConnection = minConnection;
    }

    public SFTPProfile getSFTPProfile() {
        return new SFTPProfile(sftpUser,sftpHost,sftpPort,sftpPassword,sftpPath);
    }

    public SFTPProfile getSFTPProfile2() {
        return new SFTPProfile(sftpUser2,sftpHost2,sftpPort2,sftpPassword2,sftpPath2);
    }

    public String getMock() {
        return mock;
    }

    public void setMock(String mock) {
        this.mock = mock;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

}
