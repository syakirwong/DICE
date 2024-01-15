package com.alliance.diceanalytics.configurer;

import com.alliance.diceanalytics.model.AuditTrail;
import com.alliance.diceanalytics.model.DataField;
import com.alliance.diceanalytics.service.AuditTrailService;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@Configuration
public class ApiAuditInterceptor implements HandlerInterceptor {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws TemplateException, IOException {

        //Success code range
        if (response.getStatus() >= 200 && response.getStatus() <= 299) {
            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setStatusCode(String.valueOf(response.getStatus()));
            auditTrail.setEndPointUrl(request.getRequestURL().toString());
            auditTrail.setHttpMethod(DataField.HttpMethod.valueOf(request.getMethod()));
            auditTrail.setEvent(((HandlerMethod) handler).getMethod().getName());
            auditTrail.setIsRetry(false);
            auditTrail.setIsSentEmail(false);
            auditTrail.setRequestStatus(DataField.RequestStatus.SUCCESS);
            auditTrail.setMessage("");
            auditTrail.setCodeLocation(((HandlerMethod) handler).getMethod().getDeclaringClass().toString());
            auditTrailService.saveAuditTrail(auditTrail);
        }


    }

}


