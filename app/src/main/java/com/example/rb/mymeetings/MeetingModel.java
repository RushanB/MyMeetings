package com.example.rb.mymeetings;

import org.joda.time.MutableDateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rush on 2017-11-27.
 */

public class MeetingModel implements Serializable {
    //meeting id
    public long id = -1;
    //variables
    public String title, note;
    //contacts
    public Set<String> contacts = new HashSet<>();
    //start and end date using joda
    public MutableDateTime startDate, endDate;

    public MeetingModel() {

    }

}
