package com.fortify.plugin.jenkins.credentials;

import java.util.regex.Pattern;

/**
 * This is a utility class to validate the tokens as Base64 encoded
 *
 * @author svijaykumar
 */
public class TokenUtil {
    private final static String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private final static Pattern UUID_REGEX_PATTERN = Pattern.compile(UUID_REGEX);
    private final static String BASE64_REGEX = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
    private final static Pattern BASE64_REGEX_PATTERN = Pattern.compile(BASE64_REGEX);

    /**
     * This function is used to check whether the given string is Base64 encoded or
     * not.
     *
     * @param inputString Input String to be checked for Base64 Encode
     * @return
     *                    <code>true</code> - if the input is Base64 encoded string
     *                    <br>
     *                    <code>false</code> - otherwise
     */
    public static boolean isBase64Encoded(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return false;
        }
        return BASE64_REGEX_PATTERN.matcher(inputString).matches();
    }

    /**
     * This function is used to check whether the given string is in UUID format or
     * not.
     *
     * @param inputString Input String to be checked for UUID Format
     * @return
     *                    <code>true</code> - if the input is UUD Format string <br>
     *                    <code>false</code> - otherwise
     */
    public static boolean isUuid(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

}