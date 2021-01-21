package com.github.react.sextant.viewpager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.github.react.sextant.recyclerview.Crime;
import com.github.react.sextant.recyclerview.CrimeFragment;
import com.github.react.sextant.recyclerview.CrimeLab;
import com.github.react.sextant.recyclerview.R;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    private static final String EXTRA_UUID =
            "com.github.react.sextant.viewpager.uuid";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crime_pager);

        //赋值
        mCrimes = CrimeLab.get(this).getmCrimeList();

        mViewPager = (ViewPager)findViewById(R.id.activity_crime_pager_view_pager);

        //动态添加Fragment arguments
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return new CrimeFragment();
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

    }

    public static Intent newIntent(Context packageContext, UUID uuid){
        Intent intent = new Intent(packageContext,CrimePagerActivity.class);
        intent.putExtra(EXTRA_UUID, uuid);
        return intent;
    }
}