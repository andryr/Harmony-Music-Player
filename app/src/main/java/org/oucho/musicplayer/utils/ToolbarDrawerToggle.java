package org.oucho.musicplayer.utils;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;


public class ToolbarDrawerToggle implements DrawerLayout.DrawerListener {
    private DrawerLayout mDrawerLayout;
    private DrawerArrowDrawable mArrowDrawable;
    private int[] mGravities;


    public ToolbarDrawerToggle(Context context, DrawerLayout drawerLayout, Toolbar toolbar, int[] gravities) {
        init(context, drawerLayout, toolbar, gravities);
    }

    private void init(final Context context, DrawerLayout drawerLayout, Toolbar toolbar, int[] gravities) {
        mDrawerLayout = drawerLayout;
        mArrowDrawable = new DrawerArrowDrawable(context);


        if (gravities == null) {
            mGravities = new int[]{Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM};
        } else {
            mGravities = gravities;
        }
        toolbar.setNavigationIcon(mArrowDrawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawers();
            }
        });
    }

    private void toggleDrawers() {
        for (int gravity : mGravities) {
            toggleDrawer(gravity);
        }
    }

    private void toggleDrawer(int gravity) {
        if (mDrawerLayout.isDrawerOpen(gravity)) {
            mDrawerLayout.closeDrawer(gravity);
        } else {
            mDrawerLayout.openDrawer(gravity);
        }
    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        mArrowDrawable.setProgress(slideOffset);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        mArrowDrawable.setProgress(1.0f);
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        mArrowDrawable.setProgress(0.0f);
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }


}
