package com.example.rb.mymeetings;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.danlew.android.joda.JodaTimeAndroid;

public class MainActivity extends AppCompatActivity implements MeetingFragment.Delegate, ViewPager.OnPageChangeListener {

    ViewPager myViewPager;
    SectionsPagerAdapter mySections;
    private FloatingActionButton floatingActionButton;
    private MeetingFragment meetingFragment;

    //load the meetings
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);

        if(savedInstanceState == null) {
            meetingFragment = new MeetingFragment();
            meetingFragment.delegate = this;
        }
        MeetingManager.getMeetingManager().setContext(getApplicationContext());
        MeetingManager.getMeetingManager().loadMeetings();

        mySections = new SectionsPagerAdapter(getSupportFragmentManager());

        myViewPager = (ViewPager) findViewById(R.id.viewPager);
        myViewPager.setAdapter(mySections);
        myViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(myViewPager);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fabLayout);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeetingFragment(null);
            }
        });
    }

    //show meeting fragment helper method
    public void showMeetingFragment(MeetingModel meetingModel) {
        ListFragment listFragment = (ListFragment)mySections.getItem(myViewPager.getCurrentItem());

        if(listFragment != null && listFragment.actionMode != null) {
            listFragment.actionMode.finish();
        }

        if(meetingModel != null) {
            meetingFragment = MeetingFragment.newInstance(meetingModel.id);
        } else {
            meetingFragment = new MeetingFragment();
        }
        meetingFragment.delegate = this;
        meetingFragment.show(getFragmentManager(),"dialog");
        floatingActionButton.hide();
    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {
        if (fragment instanceof  MeetingFragment) {
            this.meetingFragment = (MeetingFragment) fragment;
            this.meetingFragment.delegate = this;
        }
    }

    //hide fragment helper method
    public void hideMeetingFragment() {
        floatingActionButton.show();
        meetingFragment.dismiss();
    }

    //delegate methods
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //show section
    @Override
    public void onPageSelected(int position) {
        for(int i=0; i<2; i++) {
            ListFragment listFragment = (ListFragment)mySections.getItem(i);

            if(listFragment != null && listFragment.actionMode != null) {
                listFragment.actionMode.finish();
            }
        }
    }

    //delegate methods
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //hide meeting method
    @Override
    public void close(){
        hideMeetingFragment();
    }

    //class for the sections
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        //today and upcoming
        ListFragment today = ListFragment.newInstance(ListFragment.ListType.TYPE_TODAY);
        ListFragment upcoming = ListFragment.newInstance(ListFragment.ListType.TYPE_ALL);

        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        //get item override
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return today;
                case 1:
                    return upcoming;
                default:
                    return upcoming;
            }
        }

        //show title
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return "Today";
                case 1:
                    return "Upcoming";
            }
            return null;
        }

        //return number of sections
        @Override
        public int getCount() {
            return 2;
        }

    }

}
