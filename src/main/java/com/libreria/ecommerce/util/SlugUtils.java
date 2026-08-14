package com.libreria.ecommerce.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private SlugUtils() {
    }

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = NON_ALPHANUMERIC.matcher(normalized.toLowerCase()).replaceAll("-");
        return slug.replaceAll("^-+|-+$", "");
    }
}
