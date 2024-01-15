package com.alliance.dicesqlitecache.request;

import java.util.List;

import lombok.Data;

@Data
public class SendCommonEmailRequest {
    private String mailSubject;
    private String mailFrom;
    private String[] mailTo;
    private String mailContent;
    private List<String> filePaths;

    public SendCommonEmailRequest() {

    }

    public SendCommonEmailRequest(String mailSubject, String mailFrom, String[] mailTo, String mailContent,
            List<String> filePaths) {
        this.mailSubject = mailSubject;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailContent = mailContent;
        this.filePaths = filePaths;
    }
}
