package com.alliance.diceanalytics.utility;

import com.alliance.diceanalytics.constant.SFTPProfile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

    //SFTP
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

    //mft
    private String mftUser;
    private String mftHost;
    private String mftPort;
    private String mftPassword;
    private String mftPath;

    //mft pull
    private String mftUserPull;
    private String mftHostPull;
    private String mftPortPull;
    private String mftPasswordPull;
    private String mftPathPull;

    //Mail
    private List<String> mailMonthlyReferralReportTo;
    private List<String> mailWeeklyPersonalInfoReportTo;
    private List<String> mailFailureHandleAlertTo;

    //Campaign Param
    private List<String> ekycSavePlusCampaignName;
    private List<String> ekycPloanCrossSellCampaignName;
    private List<String> ekycPloanSoloCCCampaignName;

    private String SECRET_KEY;
    private String SALT;

    @Autowired
    public SystemParam(@Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
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

                       @Value("${sftp.mft.user}") String mftUser,
                       @Value("${sftp.mft.host}") String mftHost,
                       @Value("${sftp.mft.port}") String mftPort,
                       @Value("${sftp.mft.path}") String mftPath,
                       @Value("${sftp.mft.password}") String mftPassword,

                       @Value("${sftp.mft.pull.user}") String mftUserPull,
                       @Value("${sftp.mft.pull.host}") String mftHostPull,
                       @Value("${sftp.mft.pull.port}") String mftPortPull,
                       @Value("${sftp.mft.pull.path}") String mftPathPull,
                       @Value("${sftp.mft.pull.password}") String mftPasswordPull,

                       @Value("${sftp.environment}") String sftpEnvironment,
                       @Value("${sftp.mock}") String sftpMock,

                       @Value("#{'${spring.mail.monthly.referral.report.to}'.split(',')}") List<String> mailMonthlyReferralReportTo,
                       @Value("#{'${spring.mail.weekly.personal.info.report.to}'.split(',')}") List<String> mailWeeklyPersonalInfoReportTo,
                       @Value("#{'${spring.mail.failure.handle.alert.to}'.split(',')}") List<String> mailFailureHandleAlertTo,

                       @Value("#{'${ekyc.saveplus.referral.campaign.name}'.split('\\|')}") List<String> ekycSavePlusCampaignName ,
                       @Value("#{'${ekyc.ploan.crosssell.campaign.name}'.split('\\|')}") List<String> ekycPloanCrossSellCampaignName,
                       @Value("#{'${ekyc.ploan.solocc.campaign.name}'.split('\\|')}") List<String> ekycPloanSoloCCCampaignName,
                       @Value("${dice.encryption.secret.key}") String SECRET_KEY,
                       @Value("${dice.encryption.salt}") String SALT


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

        SystemParam.getInstance().setMftUser(mftUser);
        SystemParam.getInstance().setMftHost(mftHost);
        SystemParam.getInstance().setMftPort(mftPort);
        SystemParam.getInstance().setMftPath(mftPath);
        SystemParam.getInstance().setMftPassword(mftPassword);

        SystemParam.getInstance().setMftUserPull(mftUserPull);
        SystemParam.getInstance().setMftHostPull(mftHostPull);
        SystemParam.getInstance().setMftPortPull(mftPortPull);
        SystemParam.getInstance().setMftPathPull(mftPathPull);
        SystemParam.getInstance().setMftPasswordPull(mftPasswordPull);

        SystemParam.getInstance().setEnvironment(sftpEnvironment);
        SystemParam.getInstance().setMock(sftpMock);

        SystemParam.getInstance().setMailMonthlyReferralReportTo(mailMonthlyReferralReportTo);
        SystemParam.getInstance().setMailWeeklyPersonalInfoReportTo(mailWeeklyPersonalInfoReportTo);
        SystemParam.getInstance().setMailFailureHandleAlertTo(mailFailureHandleAlertTo);


        SystemParam.getInstance().setEkycSavePlusCampaignName(ekycSavePlusCampaignName);
        SystemParam.getInstance().setEkycPloanCrossSellCampaignName(ekycPloanCrossSellCampaignName);
        SystemParam.getInstance().setEkycPloanSoloCCCampaignName(ekycPloanSoloCCCampaignName);

        SystemParam.getInstance().setSECRET_KEY(SECRET_KEY);
        SystemParam.getInstance().setSALT(SALT);

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

    public SFTPProfile getSFTPProfile() {
        return new SFTPProfile(sftpUser,sftpHost,sftpPort,sftpPassword,sftpPath);
    }

    public SFTPProfile getSFTPProfile2() {
        return new SFTPProfile(sftpUser2,sftpHost2,sftpPort2,sftpPassword2,sftpPath2);
    }

    public SFTPProfile getMftSFTPProfile() {
        return new SFTPProfile(mftUser,mftHost,mftPort,mftPassword,mftPath);
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

    public List<String> getMailMonthlyReferralReportTo() {
        return this.mailMonthlyReferralReportTo;
    }

    public void setMailMonthlyReferralReportTo(List<String> mailMonthlyReferralReportTo) {
        this.mailMonthlyReferralReportTo = mailMonthlyReferralReportTo;
    }

    public List<String> getMailWeeklyPersonalInfoReportTo() {
        return this.mailWeeklyPersonalInfoReportTo;
    }

    public void setMailWeeklyPersonalInfoReportTo(List<String> mailWeeklyPersonalInfoReportTo) {
        this.mailWeeklyPersonalInfoReportTo = mailWeeklyPersonalInfoReportTo;
    }


}
