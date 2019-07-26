package com.munch.module.test;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.munch.lib.image.ImageHelper;
import com.munch.lib.image.glide.GlideStrategy;
import com.munch.module.test.fragments.*;
import com.munhc.lib.libnative.root.RootActivity;

public class MainActivity extends RootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageHelper.setupStrategy(new GlideStrategy());

        ViewPager viewPager = findViewById(R.id.vp);
        TabLayout tab = findViewById(R.id.tl);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                position++;
                switch (position) {
                    case 1:
                        return new Fragment1();
                    case 2:
                        return new Fragment2();
                    case 3:
                        return new Fragment3();
                    case 4:
                        return new Fragment4();
                    case 5:
                        return new Fragment5();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 5;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                position++;
                switch (position) {
                    case 1:
                        return "首页";
                    case 2:
                        return "会员";
                    case 3:
                        return "主页";
                    case 4:
                        return "其它";
                    case 5:
                        return "我的";
                }
                return null;
            }
        });

        tab.setupWithViewPager(viewPager);
    }

    @Override
    public boolean judgeHandleByFragment() {
        return true;
    }
}
