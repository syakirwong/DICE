package com.alliance.diceetl.config.springbatch;

import com.alliance.diceetl.config.springbatchlistener.JobListener;
import com.alliance.diceetl.config.springbatchlistener.StepListener;
import com.alliance.diceetl.constants.BatchConfigConstants;
import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.entity.PloanApplication;
import com.alliance.diceetl.repository.PloanApplicationRepository;
import com.alliance.diceetl.rowmapper.PloanApplicationRowMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
public class PloanApplicationBatchConfig {

    @Autowired
    private PloanApplicationRepository ploanApplicationRepository;

    @Autowired
    @Qualifier("plDbDataSource")
    private DataSource plDbDataSource;

    @Bean
    public JdbcPagingItemReader<PloanApplication> ploanApplicationReader() throws Exception {
        JdbcPagingItemReader<PloanApplication> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(plDbDataSource);
        reader.setPageSize(BatchConfigConstants.PAGE_SIZE);
        reader.setRowMapper(new PloanApplicationRowMapper());

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setSelectClause(BatchConfigConstants.PloanApplication.SELECT_CLAUSE);
        queryProvider.setFromClause(BatchConfigConstants.PloanApplication.FROM_CLAUSE);
        queryProvider.setWhereClause(BatchConfigConstants.PloanApplication.WHERE_CLAUSE);
        queryProvider.setSortKey(BatchConfigConstants.PloanApplication.SORT_KEY);
        queryProvider.setDataSource(plDbDataSource);
        queryProvider.setDatabaseType(Database.DB2.name());
        reader.setQueryProvider(Objects.requireNonNull(queryProvider.getObject()));

        return reader;
    }

    @Bean
    public RepositoryItemWriter<PloanApplication> ploanApplicationWriter(){
        return new RepositoryItemWriterBuilder<PloanApplication>()
                .repository(ploanApplicationRepository)
                .methodName("save")
                .build();
    }

    @Bean(name = "ploanApplicationJob")
    public Job ploanApplicationJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder(BatchConstants.JobNames.PLOAN_APPLICATION,jobRepository)
                .flow(ploanApplicationStep(jobRepository,platformTransactionManager))
                .end()
                .listener(new JobListener())
                .build();
    }

    @Bean
    public Step ploanApplicationStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder(BatchConstants.StepNames.PLOAN_APPLICATION,jobRepository)
                .<PloanApplication,PloanApplication>chunk(BatchConfigConstants.CHUNK_SIZE,platformTransactionManager)
                .reader(ploanApplicationReader())
                .writer(ploanApplicationWriter())
                .taskExecutor(taskExecutor())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .listener(new StepListener())
                .allowStartIfComplete(true)
                .build();
    }


    @Bean(name = "ploanTaskExecutor")
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(BatchConfigConstants.CONCURRENCY_LIMIT);
        return simpleAsyncTaskExecutor;
    }

}
