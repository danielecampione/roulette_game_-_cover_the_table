package it.campione.roulette;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    private static final String BUNDLE_NAME = "MessagesBundle";
    private static ResourceBundle resourceBundle;

    public static void setLocale(Locale locale) {
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
}
