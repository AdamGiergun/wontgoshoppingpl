package eu.adamgiergun.wontgoshoppingpl.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.*;

import java.util.Iterator;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.wontgoshoppingpl.R;

import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.COMMERCE_SUNDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.HOLIDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.NON_COMMERCE_DAY;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "events.db";
    private static final String NULL_VALUE = null; //used to avoid some disambiguation in ContentValues.put
    private Resources resources;

    private DatabaseHandler(Context appContext) {
        super(appContext, DATABASE_NAME, null, DATABASE_VERSION);
        resources = appContext.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.SQL_CREATE_EVENTS);
        db.execSQL(DBContract.SQL_CREATE_REMINDERS);
        //fillDbWithEvents(db, resources, 2019);
        fillDbWithEvents(db, resources, 2020);
        fillDbWithEvents(db, resources, 2021);
        fillDbWithEvents(db, resources, 2022);
        fillDbWithEvents(db, resources, 2023);
        resources = null;
    }

    private static void fillDbWithEvents(SQLiteDatabase db, Resources resources, int year) {
        Iterator<EventsTableRow> iterator = newEventsIterator(resources, year);
        while (iterator.hasNext()) {
            insertRowInDb(iterator.next(), db);
        }
    }

    private static Iterator<EventsTableRow> newEventsIterator(Resources resources, int year) {
        EventsTableRow.Set rows = new EventsTableRow.Set();
        int yesterdayHash = SimpleDate.getYesterday().hashCode();
        for (int arrayId : getArrays(year)) {
            String[] array = resources.getStringArray(arrayId);
            int typeCode = getDayTypeCode(arrayId);
            for (String item : array) {
                EventsTableRow row = EventsTableRow.newRow(item, typeCode);
                if (row.dateHash > yesterdayHash) {
                    rows.add(row);
                }
            }
        }
        //Collections.sort(rows);
        return rows.iterator();
    }

    private static int[] getArrays(int year) {
        switch (year) {
            case 2020:
                return new int[]{R.array.non_commerce_days_2020, R.array.commerce_sundays_2020, R.array.holidays_2020};
            case 2021:
                return new int[]{R.array.non_commerce_days_2021, R.array.commerce_sundays_2021, R.array.holidays_2021};
            case 2022:
                return new int[]{R.array.non_commerce_days_2022, R.array.commerce_sundays_2022, R.array.holidays_2022};
            case 2023:
                return new int[]{R.array.non_commerce_days_2023, R.array.commerce_sundays_2023, R.array.holidays_2023};
            default:
                return new int[0];
        }
    }

    private static int getDayTypeCode(int idOfArray) {
        switch (idOfArray) {
            case R.array.holidays_2020:
            case R.array.holidays_2021:
            case R.array.holidays_2022:
            case R.array.holidays_2023:
                return HOLIDAY.getCode();
            case R.array.non_commerce_days_2020:
            case R.array.non_commerce_days_2021:
            case R.array.non_commerce_days_2022:
            case R.array.non_commerce_days_2023:
                return NON_COMMERCE_DAY.getCode();
            case R.array.commerce_sundays_2020:
            case R.array.commerce_sundays_2021:
            case R.array.commerce_sundays_2022:
            case R.array.commerce_sundays_2023:
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
        switch (oldVersion) {
            case 1:
                putPack1(db);
                putPack2(db);
                break;
            case 2:
                putPack2(db);
                break;
            default:
                break;
        }
        resources = null;
    }

    private void putPack1(SQLiteDatabase db) {
        fillDbWithEvents(db, resources, 2020);
    }

    private void putPack2(SQLiteDatabase db) {
        fillDbWithEvents(db, resources, 2021);
        fillDbWithEvents(db, resources, 2022);
        fillDbWithEvents(db, resources, 2023);
    }
}
