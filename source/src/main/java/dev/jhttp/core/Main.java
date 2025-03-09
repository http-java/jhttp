package dev.jhttp.core;

import org.jetbrains.annotations.NotNull;

public class Main {

    public static void main(String[] args) {
        System.out.println(test("name"));
    }

    public static boolean test(@NotNull String name) {
        // Verifications
        if (name.isEmpty()) {
            throw new IllegalArgumentException("header name cannot be null");
        } else if (!Character.isLetter(name.charAt(0))) {
            throw new IllegalArgumentException("the header key name must start with a letter");
        } else {
            char[] chars = name.toCharArray();

            for (int row = 0; row < chars.length; row++) {
                char c = chars[row];

                if (!Character.isLetterOrDigit(c) && c != '-') {
                    throw new IllegalStateException("the header key name contains an illegal character at index " + row);
                }
            }
        }

        return true;
    }

}
