package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.risky_patient.RoutineFUpFragment;
import org.smartregister.brac.hnpp.fragment.risky_patient.SpecialFUpFragment;
import org.smartregister.brac.hnpp.fragment.risky_patient.TelephonicFUpFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.telephonic_followup,R.string.special_followup,R.string.routine_f_up};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment.
        switch (position){
            case 0:
                return TelephonicFUpFragment.newInstance(position+1);
            case 1:
                return SpecialFUpFragment.newInstance(position + 1);
            default:
                return RoutineFUpFragment.newInstance(position + 1);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}