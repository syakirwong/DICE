package com.alliance.dicenotification.constant;

import com.alliance.dicenotification.utility.SystemParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

@ConstructorBinding
@Component
@Data
public class SFTPConstant {

    public static SFTPConstant constant = null;

    public SFTPProfile profile;

    public SFTPProfile profile2;

    public String environment;
    public String mock;
    
    public static SFTPConstant getInstance() {

        if (constant == null) {
            constant = new SFTPConstant();
            try {
                constant.init();
            } catch (ClassNotFoundException e) {
                System.out.println("SFTPConstant issue : " + e.getMessage());
            }
        }
        return constant;
    }

    public void init() throws ClassNotFoundException {
        this.profile = SystemParam.getInstance().getSFTPProfile();
        this.profile2 = SystemParam.getInstance().getSFTPProfile2();

        String environment = SystemParam.getInstance().getEnvironment();
        String mock = SystemParam.getInstance().getMock();

        setEnvironment(environment);
        setMock(mock);
    }
   
    public static void setConnectionNull() {
        SFTPConstant.constant = null;
    }

}