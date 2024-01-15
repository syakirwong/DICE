package com.alliance.dicerecommendation.utility;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

@Component
public class FreemarkerUtil {
    @Autowired
    private Configuration fmConfiguration;

    public String getEmailContentFromTemplate(Map<String, Object> mailInfo) throws IOException, TemplateException {
		return FreeMarkerTemplateUtils.processTemplateIntoString(
				fmConfiguration.getTemplate(String.valueOf(mailInfo.get("emailTemplate"))), mailInfo);
	}
}
