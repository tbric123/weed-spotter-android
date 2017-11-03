package knightwing.ws.weedspotter.Views.Widgets;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import knightwing.ws.weedspotter.Views.PlantIdentification.NotesFragment;
import knightwing.ws.weedspotter.Views.PlantIdentification.PhotoPromptFragment;
import knightwing.ws.weedspotter.Views.PlantIdentification.PlantInfoFragment;

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
        switch(position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return PhotoPromptFragment.newInstance(position, position, getCount());
            case 6:
                return PlantInfoFragment.newInstance(position, getCount());
            case 7:
                return NotesFragment.newInstance(position, getCount());
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // Show 8 total pages.
        return 8;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "SECTION 1";
            case 1:
                return "SECTION 2";
            case 2:
                return "SECTION 3";
            case 3:
                return "SECTION 4";
            case 4:
                return "SECTION 5";
            case 5:
                return "SECTION 6";
            case 6:
                return "SECTION 7";
            case 7:
                return "SECTION 8";

        }
        return null;
    }

}