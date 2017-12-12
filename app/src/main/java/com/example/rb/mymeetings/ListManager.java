package com.example.rb.mymeetings;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rush on 2017-11-27.
 */

public class ListManager extends ArrayAdapter<MeetingModel> {

    CardView myView;
    TextView textViewTitle, textViewStartTime, textViewTimeHour, textViewTimeHalf;
    ListFragment.ListType listType;

    public ListManager(Context context, int resource, List<MeetingModel> models, ListFragment.ListType listType) {
        super(context,resource,models);
        this.listType = listType;
    }

    //return view for each view group at a position
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        MeetingModel meetingModel = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_meeting,viewGroup, false);
        }

        myView = (CardView) view.findViewById(R.id.meetingView);
        textViewTitle = (TextView) view.findViewById(R.id.titleTextView);
        textViewStartTime = (TextView) view.findViewById(R.id.startTimeTextView);
        textViewTimeHour = (TextView) view.findViewById(R.id.dateTextView);
        textViewTimeHalf = (TextView) view.findViewById(R.id.timeTextView);

        textViewTitle.setText(meetingModel.title);

        String timeString = meetingModel.startDate.toString("h:mm a") + " - " + meetingModel.endDate.toString("h:mm a");
        textViewStartTime.setText(timeString);

        if (listType != ListFragment.ListType.TYPE_ALL) {
            textViewTimeHour.setText(meetingModel.startDate.toString("h"));
            textViewTimeHalf.setText(meetingModel.startDate.toString("a"));
        } else {
            textViewTimeHour.setText(meetingModel.startDate.toString("d"));
            textViewTimeHalf.setText(meetingModel.startDate.toString("MMM"));
        }

        return view;
    }

}
