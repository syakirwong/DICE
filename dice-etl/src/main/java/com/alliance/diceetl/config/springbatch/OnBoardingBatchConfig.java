package com.alliance.diceetl.config.springbatch;

import com.alliance.diceetl.config.springbatchlistener.JobListener;
import com.alliance.diceetl.config.springbatchlistener.StepListener;
import com.alliance.diceetl.constants.BatchConfigConstants;
import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.entity.OnBoardingForms;
import com.alliance.diceetl.repository.OnBoardingFormsRepository;
import com.alliance.diceetl.rowmapper.OnBoardingFormsRowMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.QueryTimeoutException;
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
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;


@Configuration
@AllArgsConstructor
@RequiredArgsConstructor
public class OnBoardingBatchConfig {

    @Autowired
    private OnBoardingFormsRepository onBoardingFormsRepository;

    @Autowired
    @Qualifier("vccDataSource")
    private DataSource vccDataSource;

    @Bean
    public JdbcPagingItemReader<OnBoardingForms> onBoardingFormReader() throws Exception {
        JdbcPagingItemReader<OnBoardingForms> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(vccDataSource);
        reader.setPageSize(BatchConfigConstants.PAGE_SIZE); // Set the page size
        reader.setRowMapper(new OnBoardingFormsRowMapper()); // Implement RowMapper

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setSelectClause(BatchConfigConstants.OnBoarding.SELECT_CLAUSE);
        queryProvider.setFromClause(BatchConfigConstants.OnBoarding.FROM_CLAUSE);
        queryProvider.setWhereClause(BatchConfigConstants.OnBoarding.WHERE_CLAUSE);
        queryProvider.setSortKey(BatchConfigConstants.OnBoarding.SORT_KEY);
        queryProvider.setDataSource(vccDataSource);
        queryProvider.setDatabaseType(Database.DB2.name());
        reader.setQueryProvider(Objects.requireNonNull(queryProvider.getObject()));

        return reader;
    }

//    @Bean
//    public JdbcCursorItemReader<OnBoardingForms> reader(){
//        return new JdbcCursorItemReaderBuilder<OnBoardingForms>()
//                .dataSource(vccDataSource)
//                .name("reader")
//                .sql("SELECT UUID,MOBILE_NO,CUSTOMER_NAME,SUBMITTED_ON,PROMO_CODE,DEVICE_UUID,DEVICE_PLATFORM FROM ONBOARDING_FORMS_VIEW")
//                .rowMapper(new OnBoardingFormsRowMapper())
//                .build();
//    }

    @Bean
    public RepositoryItemWriter<OnBoardingForms> writer(){
        return new RepositoryItemWriterBuilder<OnBoardingForms>()
                .repository(onBoardingFormsRepository)
                .methodName("save")
                .build();
    }

    @Bean(name = "onBoardingFormsJob")
    public Job onBoardingFormsJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder(BatchConstants.JobNames.ONBOARDING_FORMS,jobRepository)
                .flow(onBoardingFormsStep(jobRepository,platformTransactionManager))
                .end()
                .listener(new JobListener())
                .build();
    }

    @Bean
    public Step onBoardingFormsStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder(BatchConstants.StepNames.ONBOARDING_FORMS,jobRepository)
                .<OnBoardingForms,OnBoardingForms>chunk(BatchConfigConstants.CHUNK_SIZE,platformTransactionManager)
                .reader(onBoardingFormReader())
                .writer(writer())
                .allowStartIfComplete(true)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .listener(new StepListener())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(BatchConfigConstants.CONCURRENCY_LIMIT);
        return asyncTaskExecutor;
    }


}
