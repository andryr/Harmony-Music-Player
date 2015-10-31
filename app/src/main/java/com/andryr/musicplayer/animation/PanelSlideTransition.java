package com.andryr.musicplayer.animation;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by Andry on 27/10/15.
 */
public class PanelSlideTransition implements SlidingUpPanelLayout.PanelSlideListener {
    
    private View mFirstView;
    private View mSecondView;
    
    public PanelSlideTransition(View firstView, View secondView)
    {
        mFirstView = firstView;
        mSecondView = secondView;
    }
    
    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (mFirstView.getVisibility() != View.VISIBLE) {
            mFirstView.setVisibility(View.VISIBLE);
        }

        if (mSecondView.getVisibility() != View.VISIBLE) {
            mSecondView.setVisibility(View.VISIBLE);
        }



        ViewHelper.setAlpha(mFirstView, 1 - slideOffset);
        ViewHelper.setAlpha(mSecondView, slideOffset);

    }

    @Override
    public void onPanelHidden(View panel) {

    }

    @Override
    public void onPanelExpanded(View panel) {

        mFirstView.setVisibility(View.GONE);
        mSecondView.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(mSecondView, 1);

    }

    @Override
    public void onPanelCollapsed(View panel) {
        mSecondView.setVisibility(View.GONE);
        mFirstView.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(mFirstView, 1);


    }

    @Override
    public void onPanelAnchored(View panel) {
        // TODO Auto-generated method stub

    }
}
