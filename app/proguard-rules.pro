# Reguły R8 dla wontgoshoppingpl.
#
# Klasy wymienione niżej są tworzone refleksyjnie po nazwie — z zasobów XML albo
# przez FragmentManager — więc R8 nie widzi do nich żadnej referencji w kodzie.
# Bez tych reguł release wywala się przy wejściu w Ustawienia.
#
# Atrybuty SourceFile/LineNumberTable ustawia już proguard-android-optimize.txt.

# Fragmenty preferencji wskazywane przez android:fragment w res/xml/pref_headers.xml
# oraz instancjonowane w SettingsActivity.onPreferenceStartFragment.
-keep class eu.adamgiergun.wontgoshoppingpl.settings.HeadersPreferenceFragment { <init>(...); }
-keep class eu.adamgiergun.wontgoshoppingpl.settings.LookPreferenceFragment { <init>(...); }
-keep class eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment { <init>(...); }

# Własne widżety Preference rozwijane z res/xml/pref_*.xml — inflater woła
# konstruktor (Context, AttributeSet).
-keep class eu.adamgiergun.mykotlinlibrary.IntegerDialogPreference { <init>(...); }
-keep class eu.adamgiergun.mykotlinlibrary.calendarmanagement.CalendarsListPreference { <init>(...); }
-keep class eu.adamgiergun.mykotlinlibrary.calendarmanagement.TimePreference { <init>(...); }

# Dialogi towarzyszące powyższym preferencjom są odtwarzane przez FragmentManager
# po zmianie konfiguracji.
-keep class eu.adamgiergun.mykotlinlibrary.IntegerPreferenceDialogFragmentCompat { <init>(...); }
-keep class eu.adamgiergun.mykotlinlibrary.calendarmanagement.TimePreferenceDialogFragmentCompat { <init>(...); }

# Widget aplikacji deklarowany w manifeście.
-keep class eu.adamgiergun.wontgoshoppingpl.widget.Widget { <init>(...); }

# Encje Room mapowane po nazwach pól.
-keep class eu.adamgiergun.wontgoshoppingpl.db.Event { *; }
-keep class eu.adamgiergun.wontgoshoppingpl.db.Reminder { *; }
