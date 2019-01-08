package eu.adamgiergun.wontgoshoppingpl;

class LocalizedConst {
    private static String wholeDay;
    private static String since;
    private static boolean set;
    private static String[] helpTexts;

    static String getWholeDay() {
        return wholeDay;
    }

    static void setWholeDay(String wholeDay) {
        LocalizedConst.wholeDay = wholeDay;
    }

    static String getSince() {
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

    static String getHelpText(int index) {
        return helpTexts[index];
    }

    static void setHelpTexts(String[] helpTexts) {
        LocalizedConst.helpTexts = helpTexts;
    }
}
