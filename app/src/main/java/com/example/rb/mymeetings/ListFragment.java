package com.example.rb.mymeetings;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rush on 2017-11-27.
 */

public class ListFragment extends Fragment implements AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {


    public ActionMode actionMode;
    List<MeetingModel> myMeetings;
    ListType listType;
    ListManager listManager;
    TextView noMeetingTextView;
    Button showPrevious;
    Boolean showAll = false;
    ListView listView;
    ArrayList<Integer> selected = new ArrayList<>();


    public ListFragment() {

    }

    public static ListFragment newInstance(ListType listType) {
        ListFragment fragment = new ListFragment();

        Bundle args = new Bundle();
        args.putSerializable("list_type", listType);
        fragment.setArguments(args);

        return fragment;
    }

    //on select show the menu
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        this.actionMode = mode;
        return true;
    }

    //which position is the item in
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked)
            selected.add(position);
        else
            selected.remove(Integer.valueOf(position));
    }

    //delegate method
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    //on click provide the options in the main menu
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteMeeting:
                delete(mode);
                return true;
            case R.id.selectAll:
                selected.clear();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, true);
                }
                return true;
            case R.id.moveMeeting:
                move(mode);
                return true;
            default:
                return false;
        }
    }

    //go back
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        selected.clear();
    }

    //update list based on list type
    @UiThread
    private void updateList() {
        myMeetings.clear();

        switch(listType) {
            case TYPE_TODAY:
                noMeetingTextView.setText("No Meetings For Today");
                myMeetings.addAll(MeetingManager.getMeetingManager().getTodaysMeetings());
                break;
            case TYPE_ALL:
                noMeetingTextView.setText("No Upcoming Meetings");
                myMeetings.addAll(MeetingManager.getMeetingManager().myMeetings.values());
                break;
        }
        List<MeetingModel> deleteList = new ArrayList<>();
        for (MeetingModel meetingModel:myMeetings) {
            if(meetingModel.endDate.isBeforeNow()){
                deleteList.add(meetingModel);
            }
        }

        if(!showAll) {
            myMeetings.removeAll(deleteList);

            if(deleteList.size() == 0) {
                showPrevious.setVisibility(View.GONE);
            } else {
                showPrevious.setVisibility(View.VISIBLE);
                showPrevious.setText("Show Previous Meetings");
            }
        } else {
            if (deleteList.size() == 0) {
                showPrevious.setVisibility(View.GONE);
            } else {
                showPrevious.setVisibility(View.GONE);
                showPrevious.setText("Hide Previous Meetings");
            }
        }
        Collections.sort(myMeetings, new Comparator<MeetingModel>() {
            @Override
            public int compare(MeetingModel lhs, MeetingModel rhs) {
                return lhs.startDate.compareTo(rhs.startDate);
            }
        });
        listManager.notifyDataSetChanged();

        if(myMeetings.size() == 0) {
            noMeetingTextView.setVisibility(View.VISIBLE);
        } else {
            noMeetingTextView.setVisibility(View.GONE);
        }
    }

    //on create show list
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_main, group, false);
        listType = (ListType) getArguments().getSerializable("list_type");
        myMeetings = new ArrayList<>();
        listManager = new ListManager(getActivity(),0,myMeetings,listType);

        noMeetingTextView = (TextView) view.findViewById(R.id.noMeetings);
        showPrevious = (Button) view.findViewById(R.id.showPrevious);
        showPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAll = !showAll;
                updateList();
            }
        });
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(listManager);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemClickListener(this);

        updateList();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateList();
            }
        }, new IntentFilter("meeting_created"));

        return view;
    }

    //returns item at a position for the meeting fragment
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showMeetingFragment(listManager.getItem(position));
    }

    //move a selected meeting to another date
    private void move(final ActionMode mode) {
        MeetingModel tempMeeting = listManager.getItem(selected.get(0));

        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                for(int i=0; i<selected.size(); i++) {
                    MeetingModel meetingModel = listManager.getItem(i);
                    meetingModel.startDate.setYear(year);
                    meetingModel.startDate.setMonthOfYear(monthOfYear+1);
                    meetingModel.startDate.setDayOfMonth(dayOfMonth);
                    meetingModel.endDate.setYear(year);
                    meetingModel.endDate.setMonthOfYear(monthOfYear+1);
                    meetingModel.endDate.setDayOfMonth(dayOfMonth);

                    MeetingManager.getMeetingManager().createMeeting(meetingModel);
                }
                mode.finish();
            }
        },tempMeeting.startDate.getYear(),tempMeeting.startDate.getMonthOfYear()-1,tempMeeting.startDate.getDayOfMonth()).show();
    }

    //delete a seelcted meeting
    private void delete(final ActionMode mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("End Selected Meetings?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Integer index: selected) {
                    MeetingManager.getMeetingManager().deleteMeeting(listManager.getItem(index));
                }
                mode.finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    public enum ListType implements Serializable {
        TYPE_TODAY, TYPE_ALL
    }
}
