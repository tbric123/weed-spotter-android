package knightwing.ws.weedspotter.Views.PlantIdentification;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Widgets.SectionsPagerAdapter;

/**
 * The entire submission process - taking photos, specifying plant details, volunteer name.
 */
public class ReportActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /**
     * Go to the next step of the weed submission process.
     * @ensure the next screen is displayed
     */
    public void nextFragment() {
        mViewPager.arrowScroll(View.FOCUS_RIGHT);
    }

    /**
     * Go to the previous step of the weed submission process.
     * @ensure the previous screen is displayed
     */
    public void previousFragment() {
        mViewPager.arrowScroll(View.FOCUS_LEFT);
    }

    /**
     * Stop back button from being able to be pressed.
     * https://stackoverflow.com/questions/20623659/disable-back-button-in-android
     */
    @Override
    public void onBackPressed() {}
}
