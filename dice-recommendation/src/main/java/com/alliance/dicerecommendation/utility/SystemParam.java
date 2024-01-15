package com.alliance.dicerecommendation.utility;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alliance.dicerecommendation.constant.SFTPProfile;

import lombok.Data;

@Service
@Data
public class SystemParam {

    private final Properties properties = new Properties();
    private static SystemParam systemParam = null;

    private String localDataCenter;
    private String entityBasePackage;
    private String hosts;
    private Integer port;
    private String keyspace;
    private String username;
    private String password;

    //mft pull
    private String mftUserPull;
    private String mftHostPull;
    private String mftPortPull;
    private String mftPasswordPull;
    private String mftPathPull;

    private String environment;
    private String mock;

    // Mail
    private List<String> mailCampaignScheduleFailureHandleAlertTo;

    @Autowired
    public SystemParam(@Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
    @Value("${spring.data.cassandra.entity-base-package}") String entityBasePackage,
    @Value("${spring.data.cassandra.contact-points}") String hosts,
    @Value("${spring.data.cassandra.port}") Integer port,
    @Value("${spring.data.cassandra.keyspace-name}") String keyspace,
    @Value("${spring.data.cassandra.username}") String username,
    @Value("${spring.data.cassandra.password}") String password,

    @Value("${sftp.mft.pull.user}") String mftUserPull,
    @Value("${sftp.mft.pull.host}") String mftHostPull,
    @Value("${sftp.mft.pull.port}") String mftPortPull,
    @Value("${sftp.mft.pull.path}") String mftPathPull,
    @Value("${sftp.mft.pull.password}") String mftPasswordPull,

    @Value("${sftp.environment}") String sftpEnvironment,
    @Value("${sftp.mock}") String sftpMock,

    @Value("#{'${spring.mail.campaign.schedule.failure.handle.alert.to}'.split(',')}") List<String> mailCampaignScheduleFailureHandleAlertTo
            ) {
        if (systemParam == null) {
            systemParam = new SystemParam();
            systemParam.init();
        }
        // this.ACTIVEMQ_BOCKER_IP = aCTIVEMQ_BOCKER_IP;
        SystemParam.getInstance().setLocalDataCenter(localDataCenter);
        SystemParam.getInstance().setEntityBasePackage(entityBasePackage);
        SystemParam.getInstance().setHosts(hosts);
        SystemParam.getInstance().setPort(port);
        SystemParam.getInstance().setKeyspace(keyspace);
        SystemParam.getInstance().setUsername(username);
        SystemParam.getInstance().setPassword(password);

        SystemParam.getInstance().setMftUserPull(mftUserPull);
        SystemParam.getInstance().setMftHostPull(mftHostPull);
        SystemParam.getInstance().setMftPortPull(mftPortPull);
        SystemParam.getInstance().setMftPathPull(mftPathPull);
        SystemParam.getInstance().setMftPasswordPull(mftPasswordPull);

        SystemParam.getInstance().setEnvironment(sftpEnvironment);
        SystemParam.getInstance().setMock(sftpMock);

        SystemParam.getInstance().setMailCampaignScheduleFailureHandleAlertTo(mailCampaignScheduleFailureHandleAlertTo);
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

    public SFTPProfile getMftPullSFTPProfile() {
        return new SFTPProfile(mftUserPull,mftHostPull,mftPortPull,mftPasswordPull,mftPathPull);
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
