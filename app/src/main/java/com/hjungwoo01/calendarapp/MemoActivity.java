package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;


public class MemoActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    public static final String[] TAB_TITLES = {"Received Memos", "Sent Memos"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        initWidgets();
        setupViewPager();
        setupTabLayout();
    }

    private void setupViewPager() {
        FragmentStateAdapter pagerAdapter = new TabViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0, true);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(TAB_TITLES[position]);
        }).attach();
    }

    private void initWidgets() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
    }

    public void newMemoAction(View view) {
        startActivity(new Intent(MemoActivity.this, NewMemoActivity.class));
    }

    public void changeOwner(View view) {
        startActivity(new Intent(MemoActivity.this, OwnerSelectionActivity.class));
    }
}
