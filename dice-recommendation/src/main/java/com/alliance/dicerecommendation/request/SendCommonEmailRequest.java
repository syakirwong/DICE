package com.alliance.dicerecommendation.request;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SendCommonEmailRequest {
    private String mailSubject;
    private String mailFrom;
    private String[] mailTo;
    private String mailContent;
    private List<String> filePaths;
    private byte[] attachmentBytes;
    private String attachmentFileName;

    public SendCommonEmailRequest() {
    }

    public SendCommonEmailRequest(String mailSubject, String mailFrom, String[] mailTo, String mailContent, List<String> filePaths) {
        this.mailSubject = mailSubject;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailContent = mailContent;
        this.filePaths = filePaths;
    }

    public SendCommonEmailRequest(String mailSubject, String mailFrom, String[] mailTo, String mailContent, byte[] attachmentBytes, String attachmentFileName) {
        this.mailSubject = mailSubject;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailContent = mailContent;
        this.attachmentBytes = attachmentBytes;
        this.attachmentFileName = attachmentFileName;
    }

}
