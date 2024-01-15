package com.alliance.dicerecommendation.constant;

import lombok.Data;

@Data
public class SFTPProfile {
    public String user;
    public String host;
    public String port;
    public String password;
    public String path;
 
    public SFTPProfile() {
    }
    
    public SFTPProfile(String user,String host,String port, String password,String path) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.password = password;
        this.path = path;
    }
}
