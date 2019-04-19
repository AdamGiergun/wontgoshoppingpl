package eu.adamgiergun.wontgoshoppingpl.db;

// TODO do sth with fillDbWithEvents()

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;

import androidx.annotation.Nullable;

import java.util.Iterator;

import eu.adamgiergun.wontgoshoppingpl.days.*;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "events.db";
    private Context context;

    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.SQL_CREATE_EVENTS);
        db.execSQL(DBContract.SQL_CREATE_REMINDERS);
        fillDbWithEvents(db);
    }

    private void fillDbWithEvents(SQLiteDatabase db) {
        Iterator<LimitedCommerceDay> iterator = ListOfLimitedCommerceDays.getIteratorFromResources(context.getResources());
        while (iterator.hasNext()) {
            insertDayInDb(iterator.next(), db);
        }
    }

    private void insertDayInDb(LimitedCommerceDay day, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(
                DBContract.Events.COLUMN_NAME_ITEM,
                day.getItem());

        contentValues.put(
                DBContract.Events.COLUMN_NAME_TYPE_ID,
                day.getTypeCode());

        db.insertOrThrow(
                DBContract.Events.TABLE_NAME,
                null,
                contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //stub for possible future upgrades
    }

    static Cursor newCursor(SQLiteDatabase db, String[] selectionArgs) {

        return db.query(
                DBContract.Events.TABLE_NAME,
                null,
                DBContract.Events.COLUMN_NAME_TYPE_ID + "  IN (?, ?, ?)",
                selectionArgs,
                null,
                null,
                null);
    }

}
