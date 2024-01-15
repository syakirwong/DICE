package com.alliance.diceetl.config.springbatch;

import com.alliance.diceetl.config.springbatchlistener.JobListener;
import com.alliance.diceetl.config.springbatchlistener.StepListener;
import com.alliance.diceetl.constants.BatchConfigConstants;
import com.alliance.diceetl.constants.BatchConstants;
import com.alliance.diceetl.entity.DdmSoleCc;
import com.alliance.diceetl.repository.DdmSoleCcRepository;
import com.alliance.diceetl.rowmapper.DdmSoleCcRowMapper;
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
public class DdmSoleCCBatchConfig {

    @Autowired
    private DdmSoleCcRepository ddmSoleCcRepository;

    @Autowired
    @Qualifier("aopDataSource")
    private DataSource aopDataSource;

    @Bean
    public JdbcPagingItemReader<DdmSoleCc> ddmSoleCCReader() throws Exception {
        JdbcPagingItemReader<DdmSoleCc> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(aopDataSource);
        reader.setPageSize(BatchConfigConstants.PAGE_SIZE);
        reader.setRowMapper(new DdmSoleCcRowMapper());

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setSelectClause(BatchConfigConstants.DdmSoleCC.SELECT_CLAUSE);
        queryProvider.setFromClause(BatchConfigConstants.DdmSoleCC.FROM_CLAUSE);
        queryProvider.setWhereClause(BatchConfigConstants.DdmSoleCC.WHERE_CLAUSE);
        queryProvider.setSortKey(BatchConfigConstants.DdmSoleCC.SORT_KEY);
        queryProvider.setDataSource(aopDataSource);
        queryProvider.setDatabaseType(Database.DB2.name());
        reader.setQueryProvider(Objects.requireNonNull(queryProvider.getObject()));

        return reader;
    }
//
//    @Bean
//    public JdbcCursorItemReader<DdmSoleCc> ddmSoleCCReader(){
//        return new JdbcCursorItemReaderBuilder<DdmSoleCc>()
//                .dataSource(aopDataSource)
//                .name("DdmSoleCcReader")
//                .sql("SELECT USERID,ID_NO,FULL_NAME,MOBILE,UNKNOWN_NUMBER,CIFNO,GENDER,EMAIL,COUNTRY,MOBILEFIRSTPLATFORMID,DEVICEPLATFORM FROM DDM_SOLE_CC_VIEW")
//                .rowMapper(new DdmSoleCcRowMapper())
//                .build();
//    }

    @Bean
    public RepositoryItemWriter<DdmSoleCc> ddmSoleCCWriter(){
        return new RepositoryItemWriterBuilder<DdmSoleCc>()
                .repository(ddmSoleCcRepository)
                .methodName("save")
                .build();
    }

    @Bean(name = "ddmSoleCCJob")
    public Job ddmSoleCCJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
        return new JobBuilder(BatchConstants.JobNames.DDM_SOLE_CC,jobRepository)
                .flow(ddmSoleCCStep(jobRepository,platformTransactionManager))
                .end()
                .listener(new JobListener())
                .build();
    }

    @Bean
    public Step ddmSoleCCStep(JobRepository jobRepository,PlatformTransactionManager platformTransactionManager) throws Exception {
        return new StepBuilder(BatchConstants.StepNames.DDM_SOLE_CC,jobRepository)
                .<DdmSoleCc,DdmSoleCc>chunk(BatchConfigConstants.CHUNK_SIZE,platformTransactionManager)
                .reader(ddmSoleCCReader())
                .writer(ddmSoleCCWriter())
                .listener(new StepListener())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .taskExecutor(taskExecutor())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean(name = "ddmSoleCCTaskExecutor")
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(BatchConfigConstants.CONCURRENCY_LIMIT);
        return simpleAsyncTaskExecutor;
    }


}
