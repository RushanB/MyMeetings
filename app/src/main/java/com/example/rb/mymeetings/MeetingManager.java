package com.example.rb.mymeetings;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.MutableDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rush on 2017-11-27.
 */

public class MeetingManager {

    private static MeetingManager meetingManager = new MeetingManager();
    public HashMap<Long, MeetingModel> myMeetings;
    private Context context;


    private MeetingManager() {
        myMeetings = new HashMap<>();
    }

    public static MeetingManager getMeetingManager() {
        return meetingManager;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //load meetings on start
    public void loadMeetings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                myMeetings.putAll(DatabaseManager.getInstance(context).loadMeetings());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));
            }
        }).start();
    }

    //create a meeting
    public void createMeeting(MeetingModel meetingModel) {
        if(meetingModel.id != -1) {
            DatabaseManager.getInstance(context).updateMeeting(meetingModel);
            myMeetings.put(meetingModel.id, meetingModel);
        } else {
            meetingModel.id = DatabaseManager.getInstance(context).createMeeting(meetingModel);
            myMeetings.put(meetingModel.id, meetingModel);
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));
    }

    //delete a meeting
    public void deleteMeeting(MeetingModel meetingModel) {
        DatabaseManager.getInstance(context).deleteMeeting(meetingModel);

        myMeetings.remove(meetingModel.id);

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));
    }

    //get meetings for todays date
    public List<MeetingModel> getTodaysMeetings() {
        List<MeetingModel> todaysMeetings = new ArrayList<>();

        for (MeetingModel meetingModel:myMeetings.values()) {
            if(DateTimeComparator.getDateOnlyInstance().compare(meetingModel.startDate, new DateTime()) == 0)
                todaysMeetings.add(meetingModel);
        }
        return todaysMeetings;
    }
}
