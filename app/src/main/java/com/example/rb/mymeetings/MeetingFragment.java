package com.example.rb.mymeetings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;


/**
 * Created by rush on 2017-11-27.
 */

public class MeetingFragment extends DialogFragment implements View.OnClickListener {

    MeetingModel meeting;
    EditText editTitleTextF, editNoteTextF;
    MutableDateTime startDate, endDate;
    LinearLayout contactslinearLayout;
    LayoutInflater layoutInflater;
    public Delegate delegate;
    private Button startDateButton, startTimeButton, endDateButton, endTimeButton, saveButton, addContactButton;

    public MeetingFragment() {

    }

    public static MeetingFragment newInstance(Long meetingId) {
        MeetingFragment fragment = new MeetingFragment();
        Bundle args = new Bundle();
        args.putSerializable("meeting_id", meetingId);
        fragment.setArguments(args);

        return fragment;
    }

    //reset the reset time
    public void reset() {
        meeting = new MeetingModel();
        Calendar calendar = Calendar.getInstance();

        if (Integer.parseInt(DateTime.now().hourOfDay().getAsString()) > 21) {
            startDate = new MutableDateTime();
            endDate = new MutableDateTime();

            startDate.addDays(1); endDate.addDays(1);
            startDate.setHourOfDay(8); endDate.setHourOfDay(9);
            startDate.setMinuteOfHour(0); endDate.setMinuteOfHour(0);
        } else {
            //get the date from the calendar
            startDate = new MutableDateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY) + 1,0,0,0);
            endDate = new MutableDateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY) + 2,0,0,0);
        }
        startDate.addMonths(1); //joda time
        endDate.addMonths(1);
    }

    //show dates
    public void refreshDates() {
        startDateButton.setText(startDate.toString("d MMMM yyyy"));
        endDateButton.setText(endDate.toString("d MMMM yyyy"));
        startTimeButton.setText(startDate.toString("h:mm a"));
        endTimeButton.setText(endDate.toString("h:mm a"));

        //if time is before now
        if (startDate.isBeforeNow()) {
            startTimeButton.setTextColor(Color.RED);
            startDateButton.setTextColor(Color.RED);
        } else {
            startDateButton.setTextColor(Color.GREEN);
            startTimeButton.setTextColor(Color.GREEN);
        }
        //if enddate is before or after startdate
        if (endDate.isBefore(startDate) || !endDate.isAfter(startDate)) {
            endTimeButton.setTextColor(Color.RED);
        } else {
            endTimeButton.setTextColor(Color.GREEN);
        }
    }

    //close the fragment
    public void close() {
        View view = getDialog().getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }

        contactslinearLayout.removeAllViews();
        editTitleTextF.getText().clear();
        editNoteTextF.getText().clear();

        new MutableDateTime(startDate.toDate().getTime());

        if(delegate != null) {
            delegate.close();
        }
    }

    //save a meeting
    private void save() {
        String title = editTitleTextF.getText().toString().trim();

        if (title.length() == 0) {
            editTitleTextF.setHint("Please fill out a Title");
            editTitleTextF.requestFocus();
        } else if (endDate.isAfter(startDate) && startDate.isAfterNow()) {
            meeting.title = title;
            meeting.note = editNoteTextF.getText().toString().trim();
            meeting.startDate = startDate.copy();
            meeting.endDate = endDate.copy();

            MeetingManager.getMeetingManager().createMeeting(meeting);
            close();
        }
    }

    //fill while editing
    private void fill() {
        editTitleTextF.setText(meeting.title);
        editNoteTextF.setText(meeting.note);

        for (String contact:meeting.contacts) {
            addContacts(contact);
        }
    }

    //adds contacts to linear layout
    private void addContacts(String name) {
        final View userView = layoutInflater.inflate(R.layout.list_contact, null);
        ImageButton removeContactButton = (ImageButton) userView.findViewById(R.id.removeContact);
        removeContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView)userView.findViewById(R.id.displayName);
                meeting.contacts.remove(textView.getText().toString());
                contactslinearLayout.removeView(userView);
            }
        });
        TextView textView = (TextView)userView.findViewById(R.id.displayName);
        textView.setText(name);
        contactslinearLayout.addView(userView);
    }

    //view
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_meeting, viewGroup, false);
        this.layoutInflater = layoutInflater;

        contactslinearLayout = (LinearLayout) view.findViewById(R.id.contactList);

        if (getArguments() != null) {
            this.meeting = MeetingManager.getMeetingManager().myMeetings.get(getArguments().getLong("meeting_id"));
            startDate = meeting.startDate.copy();
            endDate = meeting.endDate.copy();
        } else if (bundle != null) {
            this.meeting = (MeetingModel) bundle.getSerializable("meeting");
            this.startDate = new MutableDateTime(bundle.getLong("startTime"));
            this.endDate = new MutableDateTime(bundle.getLong("endTime"));

            for (String contact: meeting.contacts) {
                addContacts(contact);
            }
        } else {
            reset();
        }
        addContactButton = (Button) view.findViewById(R.id.addContact);
        saveButton = (Button) view.findViewById(R.id.save);
        startDateButton = (Button) view.findViewById(R.id.startDate);
        endDateButton = (Button) view.findViewById(R.id.endDate);
        startTimeButton = (Button) view.findViewById(R.id.startTime);
        endTimeButton = (Button) view.findViewById(R.id.endTime);
        ImageButton closeButton = (ImageButton) view.findViewById(R.id.close);

        addContactButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        startDateButton.setOnClickListener(this);
        endDateButton.setOnClickListener(this);
        startTimeButton.setOnClickListener(this);
        endTimeButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        editTitleTextF = (EditText) view.findViewById(R.id.editTitle);
        editNoteTextF = (EditText) view.findViewById(R.id.editNote);

        if (this.meeting.id != -1) {
            fill();
        }

        refreshDates();

        return view;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

    }

    //on save
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable("meeting", meeting);
        bundle.putLong("startTime", startDate.toDate().getTime());
        bundle.putLong("endTime", endDate.toDate().getTime());
    }

    //close on cancel
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        close();
    }

    //switch for each click on the form
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addContact:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent,1);
                break;
            case R.id.save:
                save();
                break;
            case R.id.close:
                close();
                break;
            case R.id.startDate:
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        startDate.year().set(year);
                        startDate.monthOfYear().set(monthOfYear+1);
                        startDate.dayOfMonth().set(dayOfMonth);
                        endDate.year().set(year);
                        endDate.monthOfYear().set(monthOfYear+1);
                        endDate.dayOfMonth().set(dayOfMonth);
                        refreshDates();
                    }
                },startDate.getYear(), startDate.getMonthOfYear()-1, startDate.getDayOfMonth()).show();
                break;
            case R.id.startTime:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startDate.hourOfDay().set(hourOfDay);
                        startDate.minuteOfHour().set(minute);
                        refreshDates();
                    }
                }, startDate.getHourOfDay(), startDate.getMinuteOfHour(), false).show();
                break;
            case R.id.endTime:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endDate.hourOfDay().set(hourOfDay);
                        endDate.minuteOfHour().set(minute);
                        refreshDates();
                    }
                },endDate.getHourOfDay(), endDate.getMinuteOfHour(), false).show();
                break;
        }
    }

    //fetch results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contacts = intent.getData();
                    Cursor cursor = getActivity().getContentResolver().query(contacts, null, null, null, null);
                    assert  cursor != null;

                    if (cursor.moveToFirst()) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                        if (!meeting.contacts.contains(name)) {
                            meeting.contacts.add(name);
                            addContacts(name);
                        }
                    }
                    cursor.close();
                }
                break;
        }
    }


    public interface Delegate {
        void close();
    }

}
