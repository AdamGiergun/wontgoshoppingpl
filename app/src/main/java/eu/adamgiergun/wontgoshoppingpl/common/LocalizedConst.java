package eu.adamgiergun.wontgoshoppingpl.common;

import java.util.Locale;

public class LocalizedConst {
    private static String wholeDay;
    private static String since;
    private static String askForLook;
    private static boolean set;
    private static Locale locale;

    public static String getWholeDay() {
        return (wholeDay == null) ? "" : wholeDay;
    }

    public static void setWholeDay(String wholeDay) {
        LocalizedConst.wholeDay = wholeDay;
    }

    public static String getSince() {
        return (since == null) ? "" : since;
    }

    public static void setSince(String since) {
        LocalizedConst.since = since;
    }

    public static boolean isSet() {
        return set;
    }

    public static void setSet() {
        LocalizedConst.set = true;
    }

    static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        LocalizedConst.locale = locale;
    }

    public static String getAskForLook() {
        return (askForLook == null) ? "" : askForLook;
    }

    public static void setAskForLook(String askForLook) {
        LocalizedConst.askForLook = askForLook;
    }
}
