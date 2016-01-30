package com.andryr.musicplayer.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.andryr.musicplayer.R;

/**
 * Created by Andry on 11/11/15.
 */
public class ThemeDialogFragment extends PreferenceDialogFragmentCompat {


    private ImageView mDarkBlueGreyButton;
    private ImageView mBlueGreyButton;
    private ImageView mBlueButton;


    private Drawable mDarkBlueGreyDrawable;
    private Drawable mDarkBlueGreySelectedDrawable;

    private Drawable mBlueGreyDrawable;
    private Drawable mBlueGreySelectedDrawable;

    private Drawable mBlueDrawable;
    private Drawable mBlueSelectedDrawable;

    private ThemePreference mPreference;

    private int mChosenTheme;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.dark_blue_grey_button:
                    mChosenTheme = ThemePreference.DARK_BLUE_GREY_THEME;
                    mDarkBlueGreyButton.setImageDrawable(mDarkBlueGreySelectedDrawable);
                    mBlueGreyButton.setImageDrawable(mBlueGreyDrawable);
                    mBlueButton.setImageDrawable(mBlueDrawable);
                    break;
                case R.id.blue_grey_button:
                    mChosenTheme = ThemePreference.BLUE_GREY_THEME;
                    mBlueGreyButton.setImageDrawable(mBlueGreySelectedDrawable);
                    mBlueButton.setImageDrawable(mBlueDrawable);
                    mDarkBlueGreyButton.setImageDrawable(mDarkBlueGreyDrawable);
                    break;
                case R.id.blue_button:
                    mChosenTheme = ThemePreference.BLUE_THEME;
                    mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                    mBlueGreyButton.setImageDrawable(mBlueGreyDrawable);
                    mDarkBlueGreyButton.setImageDrawable(mDarkBlueGreyDrawable);
                    break;
            }

        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = (ThemePreference) getPreference();
        mChosenTheme = mPreference.getValue();
    }

    @Override
    protected void onBindDialogView(View view) {
        mDarkBlueGreyButton = (ImageView) view.findViewById(R.id.dark_blue_grey_button);
        mBlueGreyButton = (ImageView) view.findViewById(R.id.blue_grey_button);
        mBlueButton = (ImageView) view.findViewById(R.id.blue_button);

        mDarkBlueGreyButton.setOnClickListener(mOnClickListener);
        mBlueGreyButton.setOnClickListener(mOnClickListener);
        mBlueButton.setOnClickListener(mOnClickListener);


        loadDrawables(view.getContext());
        initButtons();
    }

    private void initButtons() {
        switch (mChosenTheme) {
            case ThemePreference.DARK_BLUE_GREY_THEME:
                mDarkBlueGreyButton.setImageDrawable(mDarkBlueGreySelectedDrawable);
                break;
            case ThemePreference.BLUE_GREY_THEME:
                mBlueGreyButton.setImageDrawable(mBlueGreySelectedDrawable);
                break;
            case ThemePreference.BLUE_THEME:
                mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                break;

        }

    }


    private void loadDrawables(Context context) {
        Resources res = context.getResources();


        mDarkBlueGreyDrawable = res.getDrawable(R.drawable.dark_blue_grey_theme_button_normal);
        mDarkBlueGreySelectedDrawable = res.getDrawable(R.drawable.dark_blue_grey_theme_button_selected);

        mBlueGreyDrawable = res.getDrawable(R.drawable.blue_grey_theme_button_normal);
        mBlueGreySelectedDrawable = res.getDrawable(R.drawable.blue_grey_theme_button_selected);

        mBlueDrawable = res.getDrawable(R.drawable.blue_theme_button_normal);
        mBlueSelectedDrawable = res.getDrawable(R.drawable.blue_theme_button_selected);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mPreference.save(positiveResult, mChosenTheme);
    }


}
