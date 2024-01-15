package com.alliance.diceruleengine.configurer;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    private final KieServices kieServices = KieServices.Factory.get();

    @Bean
    CustomKieContainer customKieContainer() {

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/rules/rules.drl",
                kieServices.getResources().newClassPathResource("rules/rules.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        ReleaseId releaseId = kieModule.getReleaseId();
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);

        kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/rules/campaign-rules.drl",
                kieServices.getResources().newClassPathResource("rules/campaign-rules.drl"));
        kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        kieModule = kb.getKieModule();
        ReleaseId campaignReleaseId = kieModule.getReleaseId();
        KieContainer campaignKieContainer = kieServices.newKieContainer(campaignReleaseId);

        CustomKieContainer customKieContainer = new CustomKieContainer(kieContainer, campaignKieContainer);

        return customKieContainer;
    }

    public class CustomKieContainer {
        private KieContainer kieContainer1;
        private KieContainer kieContainer2;

        public CustomKieContainer(KieContainer kieContainer1, KieContainer kieContainer2) {
            this.kieContainer1 = kieContainer1;
            this.kieContainer2 = kieContainer2;
        }

        public KieContainer getKieContainer1() {
            return kieContainer1;
        }

        public KieContainer getKieContainer2() {
            return kieContainer2;
        }
    }

}
