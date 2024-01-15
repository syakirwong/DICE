package com.alliance.dicesqlitecache.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.alliance.dicesqlitecache.repository.CacheRepository;
import com.alliance.dicesqlitecache.repository.InternetBankingActivationRepository;
import com.alliance.dicesqlitecache.repository.PloanApplicationRepository;
import com.alliance.dicesqlitecache.repository.SoleCcRepository;
import com.alliance.dicesqlitecache.repository.VccOnboardingFormsRepository;
import com.alliance.dicesqlitecache.request.SendCommonEmailRequest;
import com.alliance.dicesqlitecache.utiity.FreemarkerUtil;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CacheDataLoader implements ApplicationRunner {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private InternetBankingActivationRepository internetBankingActivationRepository;

    @Autowired
    private PloanApplicationRepository ploanApplicationRepository;

    @Autowired
    private SoleCcRepository soleCcRepository;

    @Autowired
    private VccOnboardingFormsRepository vccOnboardingFormsRepository;

    @Autowired
    private DB2DataService db2DataService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private FreemarkerUtil freemarkerUtil;

    @Value("${freemarker.email.template.name.failure.handle.alert}")
    private String DAILY_CACHE_ETL_EMAIL_TEMPLATE_NAME;

    @Value("#{'${spring.mail.failure.handle.alert.to}'.split(',')}")
    private List<String> DAILY_CACHE_ETL_EMAIL_TO;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started, refreshing cache...");
        loadDataFromDB2("all");

    }

    @Caching(evict = {
            @CacheEvict(value = "cacheEntries", allEntries = true),
            @CacheEvict(value = "profileEntries", allEntries = true)
    })
    public void loadDataFromDB2(String eventType) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = null;

        try {
            log.info("Data load task started at: {}", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info("Data load task 1: Clearing data in SQLite database and evict cache");

            switch (eventType.toLowerCase()) {
                case "internetbanking":
                    internetBankingActivationRepository.deleteAll();
                    log.info("Data load task 2: Fetch and store InternetBankingActivationEntity data");

                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromInternetBankingActivationDB2,
                            internetBankingActivationRepository);
                    break;
                case "ploanapplication":
                    ploanApplicationRepository.deleteAll();
                    log.info("Data load task 2: Fetch and store PloanApplicationEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromPloanApplicationDB2,
                            ploanApplicationRepository);
                    break;
                case "solecc":
                    soleCcRepository.deleteAll();
                    log.info("Data load task 2: Fetch and store SoleCcEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromSoleCcDB2,
                            soleCcRepository);
                    break;
                case "vcconboardingforms":
                    vccOnboardingFormsRepository.deleteAll();
                    log.info("Data load task 2: Fetch and store VccOnboardingFormsEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromVccOnboardingFormsDB2,
                            vccOnboardingFormsRepository);
                    break;
                case "all":
                    clearCacheAndSQLite();
                    log.info("Data load task 2: Fetch and store InternetBankingActivationEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromInternetBankingActivationDB2,
                            internetBankingActivationRepository);
                    log.info("Data load task 3: Fetch and store PloanApplicationEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromPloanApplicationDB2,
                            ploanApplicationRepository);
                    log.info("Data load task 4: Fetch and store SoleCcEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromSoleCcDB2,
                            soleCcRepository);
                    log.info("Data load task 5: Fetch and store VccOnboardingFormsEntity data");
                    loadDataAndStoreInSQLite(
                            db2DataService::fetchDataFromVccOnboardingFormsDB2,
                            vccOnboardingFormsRepository);
                    break;
                default:
                    log.warn("Invalid source type: {}", eventType);
            }

            log.info("Data load task: Cache loaded successfully.");
        } catch (Exception e) {
            log.error("An error occurred during data loading (loadDataFromDB2): {}", e.getMessage(), e);

            endTime = LocalDateTime.now();
            SendCommonEmailRequest request = createEmailRequest(eventType, startTime, endTime, e.getMessage());
            messagingService.sendEmail(Arrays.asList(request));
        } finally {
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            log.info("Data load task ended at: {}", endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info("Data load task duration: {} milliseconds",
                    java.time.Duration.between(startTime, endTime).toMillis());
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "cacheEntries", allEntries = true),
            @CacheEvict(value = "profileEntries", allEntries = true)
    })
    public void clearCacheAndSQLite() {
        cacheRepository.deleteAll();
        internetBankingActivationRepository.deleteAll();
        ploanApplicationRepository.deleteAll();
        soleCcRepository.deleteAll();
        vccOnboardingFormsRepository.deleteAll();

    }

    private <T> void loadDataAndStoreInSQLite(Supplier<List<T>> fetchDataSupplier, JpaRepository<T, ?> repository) {
        List<T> viewData = fetchDataSupplier.get();
        repository.saveAll(viewData);
    }

    private SendCommonEmailRequest createEmailRequest(String eventType, LocalDateTime startDateTime,
            LocalDateTime endDateTime, String exception) {
        SendCommonEmailRequest emailRequest = new SendCommonEmailRequest();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        Map<String, Object> mailInfo = new HashMap<String, Object>();
        mailInfo.put("emailTemplate", DAILY_CACHE_ETL_EMAIL_TEMPLATE_NAME);
        mailInfo.put("title", messageSource.getMessage("spring.mail.subject.failure.handle",
                new String[] { currentDate }, Locale.ENGLISH));

        try {
            emailRequest.setMailTo(DAILY_CACHE_ETL_EMAIL_TO.toArray(new String[0]));
            emailRequest.setMailFrom(null);
            emailRequest.setMailContent(
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{eventType}", eventType));
            emailRequest.setMailContent(
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{processStartTime}",
                            startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))));
            emailRequest.setMailContent(
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{processEndTime}",
                            endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))));
            emailRequest.setMailContent(
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{duration}",
                            String.valueOf(java.time.Duration.between(startDateTime, endDateTime).toMillis())));
            emailRequest.setMailContent(
                    freemarkerUtil.getEmailContentFromTemplate(mailInfo).replace("{exception}", exception));
            emailRequest.setMailSubject("[DICE CACHE] ETL Process DB2->Sqlite Error");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

        return emailRequest;
    }

}
