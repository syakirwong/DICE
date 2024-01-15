package com.alliance.dicecampaign.utility;

import java.io.InputStream;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Service;

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

    @Autowired
    public SystemParam(@Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
    @Value("${spring.data.cassandra.entity-base-package}") String entityBasePackage,
    @Value("${spring.data.cassandra.contact-points}") String hosts,
    @Value("${spring.data.cassandra.port}") Integer port,
    @Value("${spring.data.cassandra.keyspace-name}") String keyspace,
    @Value("${spring.data.cassandra.username}") String username,
    @Value("${spring.data.cassandra.password}") String password
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
}
