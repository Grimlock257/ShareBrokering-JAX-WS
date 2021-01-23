package io.grimlock257.sccc.sharebrokering.util;

/**
 * StringUtil
 *
 * This class contains functions for dealing with Strings
 *
 * @author Adam Watson
 */
public class StringUtil {

    /**
     * Private constructor as this utility class shouldn't be instantiated
     */
    private StringUtil() {
    }

    /**
     * Check whether the provided string is <b>not</b> null or of 0 length
     *
     * @param string The string to check
     * @return True when the provided string has a value
     */
    public static boolean isNotNullOrEmpty(String string) {
        return !(string == null || string.trim().length() == 0);
    }

    /**
     * Check whether the provided string <b>is</b> null or of 0 length
     *
     * @param string The string to check
     * @return True when the provided string does <b>not</b> have a value
     */
    public static boolean isNullOrEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }

    /**
     * Check whether a string contains a another string, regardless of case
     *
     * @param parentString The string that will be searched in
     * @param childString The string that will be searched for
     * @return True when the parentString contains the childString
     */
    public static boolean containsIgnoreCase(String parentString, String childString) {
        return parentString.toLowerCase().contains(childString.toLowerCase());
    }
}
