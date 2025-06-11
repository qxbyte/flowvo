package org.xue.agents.util;

import java.security.SecureRandom;

public class IdUtil {
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RND = new SecureRandom();

    public static String randomId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(RND.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}
