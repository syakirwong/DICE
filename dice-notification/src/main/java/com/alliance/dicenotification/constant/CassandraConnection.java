package com.alliance.dicenotification.constant;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

import com.alliance.dicenotification.utility.SystemParam;
import com.datastax.oss.driver.api.core.CqlSession;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@ConstructorBinding
@Component
@Data
@Slf4j
public class CassandraConnection {
    
    public static CassandraConnection connection = null;

    public CqlSession session;

    public static CassandraConnection getInstance() {
        if (connection == null) {
            try {
                connection = new CassandraConnection();
                connection.init();
                
            } catch (Exception ex) {
                log.error("Cassandra getInstance - Exception: {}" + ex.getMessage());
            }
        }
        return connection;
    }

    public void init() throws ClassNotFoundException {
        try {
            CqlSession initSession = CqlSession.builder().withLocalDatacenter(SystemParam.getInstance().getLocalDataCenter())
            .addContactPoint(InetSocketAddress.createUnresolved(SystemParam.getInstance().getHosts(), SystemParam.getInstance().getPort()))
            .withKeyspace(SystemParam.getInstance().getKeyspace()).withAuthCredentials(SystemParam.getInstance().getUsername(), SystemParam.getInstance().getPassword())
            .build();

            if (initSession != null) {
                log.info("//----Connected to Cassandra----//");
                connection.setSession(initSession);
            }

        } catch (Exception ex) {
            log.error("Cassandra init - Exception: {}" + ex.getMessage());
        } 

    }

    public static void setConnectionNull() {
        CassandraConnection.connection = null;
    }
 
}
