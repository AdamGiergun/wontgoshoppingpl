package eu.adamgiergun.wontgoshoppingpl;

import java.util.Locale;

class LocalizedConst {
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

    static void setWholeDay(String wholeDay) {
        LocalizedConst.wholeDay = wholeDay;
    }

    static String getSince() {
        if (wholeDay == null)
            return "";
        else
            return since;
    }

    static void setSince(String since) {
        LocalizedConst.since = since;
    }

    static boolean isSet() {
        return set;
    }

    static void setSet() {
        LocalizedConst.set = true;
    }

    static Locale getLocale() {
        return locale;
    }

    static void setLocale(Locale locale) {
        LocalizedConst.locale = locale;
    }

    static String getAskForLook() {
        if (wholeDay == null)
            return "";
        else
            return askForLook;
    }

    static void setAskForLook(String askForLook) {
        LocalizedConst.askForLook = askForLook;
    }
}
