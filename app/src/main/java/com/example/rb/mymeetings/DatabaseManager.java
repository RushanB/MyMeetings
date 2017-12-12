package com.example.rb.mymeetings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.MutableDateTime;

import java.util.HashMap;

/**
 * Created by rush on 2017-11-27.
 */

public class DatabaseManager extends SQLiteOpenHelper {

    //database variables
    private static final String DATABASE_NAME = "meetings";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_MEETINGS = "meetings";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_NOTE = "note";
    private static final String KEY_CONTACT_ID = "contact_id";
    private static final String KEY_MEETING_ID = "meeting_id";
    private static final String KEY_START_TIME = "starttime";
    private static final String KEY_END_TIME = "endtime";

    //meetings table
    private static final String CREATE_TABLE_MEETINGS = "CREATE TABLE "
            + TABLE_MEETINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TITLE + " TEXT,"
            + KEY_NOTE + " TEXT,"
            + KEY_START_TIME + " DATETIME,"
            + KEY_END_TIME + " DATETIME" + ")";

    //contacts table
    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE "
            + TABLE_CONTACTS + "(" + KEY_MEETING_ID + " INTEGER,"
            + KEY_CONTACT_ID + " TEXT" + ")";

    private static DatabaseManager ourInstance;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DatabaseManager(context);
        }

        return ourInstance;
    }

    //create the tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEETINGS);
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    //if tables exist
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEETINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(db);
    }

    //if lower version
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //INSERT
    public Long createMeeting(MeetingModel meetingModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_TITLE, meetingModel.title);
        contentValues.put(KEY_NOTE, meetingModel.note);
        contentValues.put(KEY_START_TIME, meetingModel.startDate.toDate().getTime());
        contentValues.put(KEY_END_TIME, meetingModel.endDate.toDate().getTime());

        Long meetingId = db.insertWithOnConflict(TABLE_MEETINGS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        for (String user:meetingModel.contacts) {
            ContentValues values = new ContentValues();
            values.put(KEY_MEETING_ID, meetingId);
            values.put(KEY_CONTACT_ID, user);
            db.insertWithOnConflict(TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return meetingId;
    }

    //DELETE
    public void deleteMeeting(MeetingModel meetingModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MEETINGS, KEY_ID + " = ?", new String[]{String.valueOf(meetingModel.id)});
        db.delete(TABLE_CONTACTS, KEY_MEETING_ID + " = ?", new String[]{String.valueOf(meetingModel.id)});
    }

    //UPDATE
    public void updateMeeting(MeetingModel meetingModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, meetingModel.id);
        values.put(KEY_TITLE, meetingModel.title);
        values.put(KEY_NOTE, meetingModel.note);
        values.put(KEY_START_TIME, meetingModel.startDate.toDate().getTime());
        values.put(KEY_END_TIME, meetingModel.endDate.toDate().getTime());

        Long meetingId = db.insertWithOnConflict(TABLE_MEETINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.delete(TABLE_CONTACTS, KEY_MEETING_ID + " = ?", new String[]{String.valueOf(meetingModel.id)}); //delete old contacts
        for (String user : meetingModel.contacts) {
            ContentValues contactValues = new ContentValues();
            contactValues.put(KEY_MEETING_ID, meetingId);
            contactValues.put(KEY_CONTACT_ID, user);
            db.insertWithOnConflict(TABLE_CONTACTS, null, contactValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    //load meetings from meetings table and contacts from contacts table
    public HashMap<Long, MeetingModel> loadMeetings() {
        HashMap<Long, MeetingModel> myMeetings = new HashMap<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEETINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MeetingModel meeting = new MeetingModel();
                meeting.id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                meeting.title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                meeting.note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
                meeting.startDate = new MutableDateTime(cursor.getLong(cursor.getColumnIndex(KEY_START_TIME)));
                meeting.endDate = new MutableDateTime(cursor.getLong(cursor.getColumnIndex(KEY_END_TIME)));

                String selectQueryContacts = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_MEETING_ID + " = " + meeting.id;
                Cursor contactCursor = db.rawQuery(selectQueryContacts, null);

                if (contactCursor.moveToFirst()) {
                    do {
                        String contactString = contactCursor.getString(contactCursor.getColumnIndex(KEY_CONTACT_ID));
                        meeting.contacts.add(contactString);
                    } while (contactCursor.moveToNext());
                }
                contactCursor.close();

                myMeetings.put(meeting.id, meeting);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return myMeetings;
    }


}

