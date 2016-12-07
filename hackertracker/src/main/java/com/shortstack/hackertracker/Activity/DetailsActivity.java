package com.shortstack.hackertracker.Activity;

import android.app.Notification;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.shortstack.hackertracker.Application.App;
import com.shortstack.hackertracker.Fragment.DescriptionDetailsFragment;
import com.shortstack.hackertracker.Model.Default;
import com.shortstack.hackertracker.R;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {



    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.time)
    TextView time;

    @Bind(R.id.location)
    TextView location;

    @Bind(R.id.category_text)
    TextView categoryText;

    @Bind(R.id.demo)
    View demo;

    @Bind(R.id.exploit)
    View exploit;

    @Bind(R.id.tool)
    View tool;

    @Bind(R.id.container)
    View container;

    @Bind(R.id.category)
    View category;









    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    private Default mItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_tab);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if( getIntent().getExtras() != null ) {
            mItem = Parcels.unwrap(getIntent().getExtras().getParcelable("item"));
        } else if ( savedInstanceState != null ) {
            mItem = savedInstanceState.getParcelable("item");
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container.setTransitionName("category");
        }

        setTitle("");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                finish();
            }
        });

        initViewPager();

        render();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);

        menu.findItem(R.id.bookmark).setIcon( isOnSchedule() ?  R.drawable.ic_bookmark_white_24dp : R.drawable.ic_bookmark_border_white_24dp );

        return true;
    }

    private void initViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(MainDetailsFragment.newInstance(mItem), "DETAILS");
        adapter.addFragment(DescriptionDetailsFragment.newInstance(mItem), "DESCRIPTION");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }






    public void render() {
        displayText();
        displaySpeakerIcons();
        displayCategory();

        updateBookmark();
    }

    public Default getContent() {
        return mItem;
    }

    private void displayCategory() {
        int count = getContent().getCategoryColorPosition();

        String[] allColors = getResources().getStringArray(R.array.colors);
        String[] allLabels = getResources().getStringArray(R.array.filter_types);

        int position = count % allColors.length;

        int color = Color.parseColor(allColors[position]);
        category.setBackgroundColor(color);

        categoryText.setText(allLabels[position]);
    }

    private boolean isOnSchedule() {
        return getContent().isBookmarked();
    }

    public void updateBookmark() {
        //bookmark.setVisibility( isOnSchedule() ? View.VISIBLE : View.GONE );
        invalidateOptionsMenu();
    }

    private void displayText() {
        title.setText(getContent().getDisplayTitle());
        time.setText(getContent().getDateStamp() + " - " + getContent().getTimeStamp(this));
        location.setText(getContent().getLocation());
    }

    private void displaySpeakerIcons() {
        tool.setVisibility(getContent().isTool() ? View.VISIBLE : View.GONE);
        exploit.setVisibility(getContent().isExploit() ? View.VISIBLE : View.GONE);
        demo.setVisibility(getContent().isDemo() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return false;

            case R.id.information:

                return true;

            case R.id.share:

                return true;

            case R.id.bookmark:
                updateSchedule();
                return true;
        }
    }


    public void updateSchedule() {


        // if not starred, star it
        if (mItem.isUnbookmarked()) {



            long notifyTime = mItem.getNotificationTimeInMillis();
            if( notifyTime > 0 ) {
                Notification notification = App.createNotification(this, mItem);
                App.scheduleNotification(notification, notifyTime, mItem.getId());
            }

            Toast.makeText(this, R.string.schedule_added, Toast.LENGTH_SHORT).show();
        } else {



            // remove alarm
            App.cancelNotification(mItem.getId());

            Toast.makeText(this, R.string.schedule_removed, Toast.LENGTH_SHORT).show();
        }



        updateBookmark();

        Logger.d("Posting event.");
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
