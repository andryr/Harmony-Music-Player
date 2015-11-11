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

    private ImageView mBlueButton;
    private ImageView mOrangeButton;


    private Drawable mOrangeDrawable;
    private Drawable mOrangeSelectedDrawable;

    private Drawable mBlueDrawable;
    private Drawable mBlueSelectedDrawable;

    private ThemePreference mPreference;

    private int mChosenTheme;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.blue_grey_button:
                    mChosenTheme = ThemePreference.BLUE_GREY_THEME;
                    mOrangeButton.setImageDrawable(mOrangeSelectedDrawable);
                    mBlueButton.setImageDrawable(mBlueDrawable);
                    break;
                case R.id.blue_button:
                    mChosenTheme = ThemePreference.BLUE_THEME;
                    mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                    mOrangeButton.setImageDrawable(mOrangeDrawable);
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
        mBlueButton = (ImageView) view.findViewById(R.id.blue_button);
        mOrangeButton = (ImageView) view.findViewById(R.id.blue_grey_button);

        mBlueButton.setOnClickListener(mOnClickListener);
        mOrangeButton.setOnClickListener(mOnClickListener);


        loadDrawables(view.getContext());
        initButtons();
    }

    private void initButtons() {
        Log.d("theme", " ee1 " + mChosenTheme);
        switch (mChosenTheme) {
            case ThemePreference.BLUE_GREY_THEME:
                mOrangeButton.setImageDrawable(mOrangeSelectedDrawable);
                break;
            case ThemePreference.BLUE_THEME:
                mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                break;

        }

    }


    private void loadDrawables(Context context) {
        Resources res = context.getResources();

        mOrangeDrawable = res.getDrawable(R.drawable.blue_grey_theme_button_normal);
        mOrangeSelectedDrawable = res.getDrawable(R.drawable.blue_grey__theme_button_selected);

        mBlueDrawable = res.getDrawable(R.drawable.blue_theme_button_normal);
        mBlueSelectedDrawable = res.getDrawable(R.drawable.blue_theme_button_selected);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mPreference.save(positiveResult, mChosenTheme);
    }


}
