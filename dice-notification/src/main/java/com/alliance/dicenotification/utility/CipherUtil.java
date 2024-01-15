package com.alliance.dicenotification.utility;

//import com.alliance.dre.model.UserInfo;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class CipherUtil {

    public static String generateSalt(String rawPassword) {
        return hash(rawPassword, BCrypt.gensalt());
    }

    public static String hash(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }

    public static boolean compare(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
