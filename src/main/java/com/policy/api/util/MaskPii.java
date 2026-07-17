package com.policy.api.util;

import org.springframework.stereotype.Component;

@Component
public class MaskPii {

    public String maskPAN(String pan) {
        if (pan == null || pan.length() != 10) {
            return pan;
        }

        return pan.substring(0, 5) + "****" + pan.substring(9);
    }

    public String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');

        if (atIndex <= 1) {
            return "*" + email.substring(atIndex);
        }

        return email.charAt(0)
                + "*".repeat(atIndex - 1)
                + email.substring(atIndex);
    }

    public String maskMobile(String mobile) {
        if (mobile == null || mobile.length() != 10) {
            return mobile;
        }

        return "******" + mobile.substring(6);
    }
}
