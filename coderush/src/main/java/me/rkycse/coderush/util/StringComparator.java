package me.rkycse.coderush.util;

public class StringComparator {



    public static boolean compareIgnoringWhitespace(String str1, String str2) {
        // Normalize the strings by removing extra whitespace
        String normalizedStr1 = normalizeString(str1);
        String normalizedStr2 = normalizeString(str2);

        // Compare the normalized strings
        return normalizedStr1.equals(normalizedStr2);
    }

    private static String normalizeString(String str) {
        // Remove leading and trailing whitespace and replace multiple whitespace characters with a single space
        return str.trim().replaceAll("\\s+", " ");
    }
}
