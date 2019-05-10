package eu.adamgiergun.wontgoshoppingpl.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.*;

import androidx.annotation.NonNull;

import java.util.Iterator;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;

import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.COMMERCE_SUNDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.HOLIDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.NON_COMMERCE_DAY;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "events.db";
    private final Resources resources;

    private DatabaseHandler(Context appContext) {
        super(appContext, DATABASE_NAME, null, DATABASE_VERSION);
        resources = appContext.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.SQL_CREATE_EVENTS);
        db.execSQL(DBContract.SQL_CREATE_REMINDERS);
        fillDbWithEvents(db, resources);
    }

    private static void fillDbWithEvents(SQLiteDatabase db, Resources resources) {
        Iterator<EventsTableRow> iterator = newEventsIterator(resources);
        while (iterator.hasNext()) {
            insertRowInDb(iterator.next(), db);
        }
    }

    private static Iterator<EventsTableRow> newEventsIterator(Resources resources) {
        EventsTableRow.Set rows = new EventsTableRow.Set();
        int yesterdayHash = SimpleDate.getYesterday().hashCode();//SimpleDate.newInstance(2019,12,20).hashCode();
        int[] arraysIds = {R.array.non_commerce_days, R.array.commerce_sundays, R.array.holidays};
        for (int arrayId : arraysIds) {
            String[] array = resources.getStringArray(arrayId);
            int typeCode = getDayTypeCode(arrayId);
            for (String item : array) {
                EventsTableRow row = new EventsTableRow(0, item, typeCode);
                if (row.dateHash > yesterdayHash) {
                    rows.add(row);
                }
            }
        }
        //Collections.sort(rows);
        return rows.iterator();
    }

    private static int getDayTypeCode(int idOfArray) {
        switch (idOfArray) {
            case R.array.holidays:
                return HOLIDAY.getCode();
            case R.array.non_commerce_days:
                return NON_COMMERCE_DAY.getCode();
            case R.array.commerce_sundays:
                return COMMERCE_SUNDAY.getCode();
            default:
                return -1;
        }
    }

    private static void insertRowInDb(EventsTableRow row, SQLiteDatabase db) {
        db.insertOrThrow(
                DBContract.Events.TABLE_NAME,
                null,
                getContentValues(row));
    }

    private static ContentValues getContentValues(EventsTableRow row) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(
                DBContract.Events.COLUMN_NAME_ITEM,
                row.item);
        contentValues.put(
                DBContract.Events.COLUMN_NAME_TYPE_ID,
                row.typeId);
        return contentValues;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //stub for possible future upgrades
    }

    public static Iterator<EventsTableRow> newEventsIterator(Context appContext, Day.Type.Set includedDayTypes) {
        DatabaseHandler dbh = new DatabaseHandler(appContext);
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cursor = newCursor(db, getSelectionArgs(includedDayTypes));

        Iterator<EventsTableRow> rowsIterator = getEventsIterator(cursor);

        if (cursor != null)
            cursor.close();
        db.close();
        dbh.close();

        return rowsIterator;
    }

    private static String[] getSelectionArgs(@NonNull Day.Type.Set includedDayTypes) {
        String[] selectionArgs = new String[includedDayTypes.size()];
        Iterator<Day.Type> iterator = includedDayTypes.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            selectionArgs[i++] = String.valueOf(iterator.next().getCode());
        }
        return selectionArgs;
    }

    private static Cursor newCursor(SQLiteDatabase db, String[] selectionArgs) {
        return db.query(
                DBContract.Events.TABLE_NAME,
                null,
                DBContract.Events.COLUMN_NAME_TYPE_ID + "  IN (?, ?, ?)",
                selectionArgs,
                null,
                null,
                null);
    }

    private static Iterator<EventsTableRow> getEventsIterator(Cursor cursor) {
        EventsTableRow.Set rows = new EventsTableRow.Set();

        if (cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                EventsTableRow row = new EventsTableRow(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getLong(4)
                );
                rows.add(row);
                cursor.moveToNext();
            }
        }
        return rows.iterator();
    }

    public static void updateEvent(int id, String calendarId, long calendarEventId, Context appContext) {
        DatabaseHandler dbh = new DatabaseHandler(appContext);
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.update(DBContract.Events.TABLE_NAME,
                getContentValues(calendarId, calendarEventId),
                DBContract.Events._ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        dbh.close();
    }

    private static ContentValues getContentValues(String calendarId, long calendarEventId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_ID, calendarId);
        if (calendarEventId > SimpleCalendarEvent.NON_EXISTING) {
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, calendarEventId);
        } else {
            String nullValue = null;
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, nullValue);
        }
        return contentValues;
    }
}
