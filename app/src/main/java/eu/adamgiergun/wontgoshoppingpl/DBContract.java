package eu.adamgiergun.wontgoshoppingpl;

import android.provider.BaseColumns;

class DBContract {

    static abstract class Events implements BaseColumns {
        static final String TABLE_NAME = "events";
        static final String COLUMN_NAME_TYPE_ID = "type_id";
        static final String COLUMN_NAME_ITEM = "item";
        static final String COLUMN_NAME_CALENDAR_ID = "calendar_id";
        static final String COLUMN_NAME_CALENDAR_EVENT_ID = "calendar_event_id";
    }

    static abstract class Reminders implements BaseColumns {
        static final String TABLE_NAME = "events_reminders";
        static final String COLUMN_NAME_EVENT_ID = "event_id";
        static final String COLUMN_NAME_HOUR = "hour";
        static final String COLUMN_NAME_MINUTE = "minute";
        static final String COLUMN_NAME_DAYS_BEFORE = "days_before";
    }

    static final String SQL_CREATE_EVENTS =
            "CREATE TABLE " + Events.TABLE_NAME + "(" +
                    Events._ID + " INTEGER PRIMARY KEY, " +
                    Events.COLUMN_NAME_ITEM + " TEXT NOT NULL, " +
                    Events.COLUMN_NAME_TYPE_ID + " INTEGER NOT NULL, " +
                    Events.COLUMN_NAME_CALENDAR_ID + " TEXT, " +
                    Events.COLUMN_NAME_CALENDAR_EVENT_ID + " INTEGER UNIQUE);";

    @SuppressWarnings("unused")
    static final String SQL_DELETE_EVENTS =
            "DROP TABLE IF EXISTS " + Events.TABLE_NAME + ";";

    static final String SQL_CREATE_REMINDERS =
            "CREATE TABLE " + Reminders.TABLE_NAME + " (" +
                    Reminders._ID + " INTEGER PRIMARY KEY, " +
                    Reminders.COLUMN_NAME_EVENT_ID + " INTEGER NOT NULL, " +
                    Reminders.COLUMN_NAME_HOUR + " INTEGER NOT NULL, " +
                    Reminders.COLUMN_NAME_MINUTE + " INTEGER NOT NULL, " +
                    Reminders.COLUMN_NAME_DAYS_BEFORE + " INTEGER NOT NULL, " +
                    " FOREIGN KEY (" + Reminders.COLUMN_NAME_EVENT_ID +
                    ") REFERENCES " + Events.TABLE_NAME + "(" + Events._ID + "));";

    @SuppressWarnings("unused")
    static final String SQL_DELETE_REMINDERS =
            "DROP TABLE IF EXISTS " + Events.TABLE_NAME + ";";
}
