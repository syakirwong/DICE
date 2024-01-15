package com.alliance.diceetl.config.springbatch;


import com.alliance.diceetl.config.springbatchlistener.JobListener;
import com.alliance.diceetl.config.springbatchlistener.StepListener;
import com.alliance.diceetl.constants.BatchConfigConstants;
import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.entity.InternetBankingActivation;
import com.alliance.diceetl.repository.InternetBankingActivationRepository;
import com.alliance.diceetl.rowmapper.InternetBankingActivationRowMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Database;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
public class InternetBankingActivationBatchConfig {


    @Autowired
    private InternetBankingActivationRepository internetBankingActivationRepository;

    @Autowired
    @Qualifier("dbobDBDataSource")
    private DataSource dbobDbDataSource;

    @Bean
    public JdbcPagingItemReader<InternetBankingActivation> internetBankingReader() throws Exception {
        JdbcPagingItemReader<InternetBankingActivation> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dbobDbDataSource);
        reader.setPageSize(BatchConfigConstants.PAGE_SIZE);
        reader.setRowMapper(new InternetBankingActivationRowMapper());

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setSelectClause(BatchConfigConstants.InternetBankingActivation.SELECT_CLAUSE);
        queryProvider.setFromClause(BatchConfigConstants.InternetBankingActivation.FROM_CLAUSE);
        queryProvider.setWhereClause(BatchConfigConstants.InternetBankingActivation.WHERE_CLAUSE);
        queryProvider.setSortKey(BatchConfigConstants.InternetBankingActivation.SORT_KEY);
        queryProvider.setDataSource(dbobDbDataSource);
        queryProvider.setDatabaseType(Database.DB2.name());
        reader.setQueryProvider(Objects.requireNonNull(queryProvider.getObject()));
        return reader;
    }


//    @Bean
//    public JdbcCursorItemReader<InternetBankingActivation> internetBankingReader(){
//        return new JdbcCursorItemReaderBuilder<InternetBankingActivation>()
//                .dataSource(dbobDbDataSource)
//                .name("InternetBankingReader")
//                .sql("SELECT UUID,ID_NO,FULL_NAME,PROMO_CODE,MOBILE,DEVICE_UUID,DEVICE_PLATFORM,COMPLETED_ON,STATUS_CODE,PDPA_FLAG FROM INTERNET_BANKING_ACTIVATION_VIEW")
//                .rowMapper(new InternetBankingActivationRowMapper())
//                .build();
//    }

    @Bean
    public RepositoryItemWriter<InternetBankingActivation> internetBankItemWriter(){
        return new RepositoryItemWriterBuilder<InternetBankingActivation>()
                .repository(internetBankingActivationRepository)
                .methodName("save")
                .build();
    }

    @Bean(name = "internetBankingActivationJob")
    public Job internetBankingActivationJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder(BatchConstants.JobNames.INTERNET_BANKING_ACTIVATION,jobRepository)
                .flow(internetBankingActivationStep(jobRepository,platformTransactionManager))
                .end()
                .listener(new JobListener())
                .build();
    }

    @Bean
    public Step internetBankingActivationStep(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder(BatchConstants.StepNames.INTERNET_BANKING_ACTIVATION,jobRepository)
                .<InternetBankingActivation,InternetBankingActivation>chunk(BatchConfigConstants.CHUNK_SIZE,platformTransactionManager)
                .reader(internetBankingReader())
                .writer(internetBankItemWriter())
                .listener(new StepListener())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .taskExecutor(taskExecutor())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = "internetBankingActivationTaskExecutor")
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(BatchConfigConstants.CONCURRENCY_LIMIT);
        return simpleAsyncTaskExecutor;
    }
}
