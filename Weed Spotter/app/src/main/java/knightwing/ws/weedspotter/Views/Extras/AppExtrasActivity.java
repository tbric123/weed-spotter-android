package knightwing.ws.weedspotter.Views.Extras;

import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import knightwing.ws.weedspotter.R;

public class AppExtrasActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static List<String> extrasContents = new ArrayList<>();
    private static final String ABOUT_CONTENT = "The Weed Spotter App allows you to email photographs " +
            "of plants to the Queensland Herbarium for identification. It has been developed to " +
            "support the <a href='https://www.qld.gov.au/environment/plants-animals/plants/herbarium/weed-spotters'>" +
            "Weed Spotters Network</a>, a joint project between the <a href='http://www.qld.gov.au/environment/plants-animals/plants/herbarium'>" +
            "<a href='http://www.qld.gov.au/environment/plants-animals/plants/herbarium'>Queensland Herbarium</a>" +
            " (DSITI), " + "<a href='http://www.daf.qld.gov.au/biosecurity'>Biosecurity Queensland</a> " +
            "(DAF) and local governments with funding support from the Land Protection Fund.<br/> " +
            "<br/><b>App User Guide</b><br/>The App User Guide can be viewed <a href='www.qld.gov.au/environment/plants-animals/plants/herbarium/weed-spotters-app-guide'>here</a>.";
    private static final String TERMS_CONTENT = "<b>Terms of Use</b><br/>You are free to download the Weed " +
            "Spotter App, use it to view App content and upload your photos to the App, so long as you " +
            "comply with the Terms of Use. You grant us a licence to use and publish any photos you upload " +
            "via the App. No warranties are given and we will not be liable to you or another person for " +
            "your download or use of the App or its contents. We may update the Terms of Use from time to " +
            "time, so you must read the Terms of Use and agree to them whenever you use the App. The full " +
            "Terms of Use can be viewed <a href='http://www.qld.gov.au/environment/plants-animals/plants/herbarium/weed-spotters-app-terms'>here</a>.<br/><br/><b>Access Information</b><br/>State of Queensland " +
            "(Department of Science, Information Technology and Innovation), 2017<br/><br/><b>About the " +
            "App</b><br/>This app was built by University of Queensland students Thomas Bricknell, Andrew " +
            "Waltenberg, Nik Aisha Dalila Binti Shamsul Anuar, Abicantya Prasidya Sophie, Matthew " +
            "Christensen and Mikaela Somerville using Android Studio.";
    private static final String PRIVACY_CONTENT = "The information you provide via the Weed Spotter " +
            "App will be managed in accordance with Queensland Government <a href='https://www.qld.gov.au/legal/privacy'>privacy requirements</a>.";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_extras);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Add references to each piece of content
        extrasContents.add(TERMS_CONTENT);
        extrasContents.add(PRIVACY_CONTENT);
        extrasContents.add(ABOUT_CONTENT);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_app_extras, container, false);
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            String extraContent = extrasContents.get(sectionNumber);

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            // Based on code from: https://stackoverflow.com/questions/17877595/i-want-text-view-as-a-clickable-link
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(extraContent, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(extraContent));
            }
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Terms";
                case 1:
                    return "Privacy";
                case 2:
                    return "About";
            }
            return null;
        }
    }
}
