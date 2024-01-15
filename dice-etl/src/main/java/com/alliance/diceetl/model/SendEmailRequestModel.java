package com.alliance.diceetl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequestModel {
    private String mailSubject;
    private String mailFrom;
    private String[] mailTo;
    private String mailContent;
    private List<String> filePaths;
}
