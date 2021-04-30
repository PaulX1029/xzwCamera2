package com.android.xzwcamera;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-19 上午11:43
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    List<Fragment> mFragments;

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    void setFragments(List<Fragment> fragments) {
        this.mFragments = fragments;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
