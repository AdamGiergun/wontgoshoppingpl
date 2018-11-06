package eu.adamgiergun.wontgoshoppingpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Iterator;

import androidx.annotation.Nullable;

class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "events_db";
    private Context context;

    DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DBContract.SQL_CREATE_EVENT_TYPES);
        db.execSQL(DBContract.SQL_CREATE_EVENTS);
        db.execSQL(DBContract.SQL_CREATE_REMINDERS);
        fillDbWithEvents(db);
    }

    private void fillDbWithEvents(SQLiteDatabase db) {
        LimitedCommerceDay day;
        Iterator<LimitedCommerceDay> iterator = ListOfLimitedCommerceDays.getIteratorFromResources(context);
        ContentValues contentValues;
        while (iterator.hasNext()) {
            day = iterator.next();
            contentValues = new ContentValues();
            contentValues.put(DBContract.Events.COLUMN_NAME_TYPE_ID,
                    day.getType().getCodeAsString());
            contentValues.put(DBContract.Events.COLUMN_NAME_NAME,
                    day.getName());
            contentValues.put(DBContract.Events.COLUMN_NAME_DATE,
                    day.getRawDate());
            db.insertOrThrow(DBContract.Events.TABLE_NAME,
                    null,
                    contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
