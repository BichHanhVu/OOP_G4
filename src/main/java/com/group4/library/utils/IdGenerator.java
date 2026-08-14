package com.group4.library.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdGenerator {

    private static final Pattern READER_ID_PATTERN = Pattern.compile("^R(\\d+)$");

    private IdGenerator() {}

    public static String nextReaderId(List<String> existingIds) {
        int max = existingIds.stream()
                .filter(id -> id != null)
                .map(READER_ID_PATTERN::matcher)
                .filter(Matcher::matches)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .max()
                .orElse(0);
        return String.format("R%03d", max + 1);
    }
}