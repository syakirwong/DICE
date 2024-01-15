package com.alliance.diceintegration.configurer;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;


@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {
	
	private final String localDataCenter;
	private final String hosts;
	private final String entityBasePackage;
	private final String keyspace;
    private final String username;
    private final String password;
	CassandraConfig(
	      @Value("${spring.data.cassandra.local-datacenter}") String localDataCenter,
	      @Value("${spring.data.cassandra.entity-base-package}") String entityBasePackage,
	      @Value("${spring.data.cassandra.contact-points}") String hosts,
	      @Value("${spring.data.cassandra.keyspace-name}") String keyspace,
          @Value("${spring.data.cassandra.username}") String username,
          @Value("${spring.data.cassandra.password}") String password) {
		this.entityBasePackage = entityBasePackage;
	    this.localDataCenter = localDataCenter;
	    this.hosts = hosts;
	    this.keyspace = keyspace;
        this.username = username;
        this.password = password;
	}
	
	@Override
	public SchemaAction getSchemaAction() {
		return SchemaAction.CREATE_IF_NOT_EXISTS;
	}
	
	@Override
	public List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keyspace)
				.with(KeyspaceOption.DURABLE_WRITES, true)
				.ifNotExists();
		
		return Arrays.asList(specification);
	}
	
	@Override
	protected String getKeyspaceName() {
		return keyspace;
	}
	
	@Override
	public String[] getEntityBasePackages() {
		return new String[] { entityBasePackage };
	}
	
	@Override
	protected String getLocalDataCenter() {
	    return localDataCenter;
	}
	
	@Override
	protected String getContactPoints() {
	    return hosts;
    }

    @Bean
    @Override
    public CqlSessionFactoryBean cassandraSession() {
        CqlSessionFactoryBean cassandraSession = super.cassandraSession();//super session should be called only once
        cassandraSession.setUsername(username);
        cassandraSession.setPassword(password);
        return cassandraSession;
    }

}
