package eu.adamgiergun.wontgoshoppingpl;

import java.util.Locale;

public class LocalizedConst {
    private static String wholeDay;
    private static String since;
    private static String askForLook;
    private static boolean set;
    private static Locale locale;

    LocalizedConst(){
        set = false;
    }

    static String getWholeDay() {
        if (wholeDay == null)
            return "";
        else
            return wholeDay;
    }

    public static void setWholeDay(String wholeDay) {
        LocalizedConst.wholeDay = wholeDay;
    }

    static String getSince() {
        if (wholeDay == null)
            return "";
        else
            return since;
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

    static void setLocale(Locale locale) {
        LocalizedConst.locale = locale;
    }

    public static String getAskForLook() {
        if (wholeDay == null)
            return "";
        else
            return askForLook;
    }

    public static void setAskForLook(String askForLook) {
        LocalizedConst.askForLook = askForLook;
    }
}
