package org.oucho.musicplayer.preferences;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.ImageView;

import org.oucho.musicplayer.R;

public class ThemeDialogFragment extends PreferenceDialogFragmentCompat {


    private ImageView original_greenButton;
    private Drawable original_greenDrawable;
    private Drawable original_greenDrawableSelected;

    private ImageView redButton;
    private Drawable redDrawable;
    private Drawable redDrawableSelected;

    private ImageView orangeButton;
    private Drawable orangeDrawable;
    private Drawable orangeDrawableSelected;

    private ImageView purpleButton;
    private Drawable purpleDrawable;
    private Drawable purpleDrawableSelected;

    private ImageView navyButton;
    private Drawable navyDrawable;
    private Drawable navyDrawableSelected;

    private ImageView blueButton;
    private Drawable blueDrawable;
    private Drawable blueDrawableSelected;

    private ImageView skyButton;
    private Drawable skyDrawable;
    private Drawable skyDrawableSelected;

    private ImageView seagreenButton;
    private Drawable seagreenDrawable;
    private Drawable seagreenDrawableSelected;

    private ImageView cyanButton;
    private Drawable cyanDrawable;
    private Drawable cyanDrawableSelected;

    private ImageView pinkButton;
    private Drawable pinkDrawable;
    private Drawable pinkDrawableSelected;


    private ThemePreference mPreference;

    private int mChosenTheme;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.original_green_button:
                    mChosenTheme = ThemePreference.original_green;

                    original_greenButton.setImageDrawable(original_greenDrawableSelected);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;
                case R.id.red_button:
                    mChosenTheme = ThemePreference.red;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawableSelected);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;
                case R.id.orange_button:
                    mChosenTheme = ThemePreference.orange;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawableSelected);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);


                    break;

                case R.id.purple_button:
                    mChosenTheme = ThemePreference.purple;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawableSelected);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.navy_button:
                    mChosenTheme = ThemePreference.navy;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawableSelected);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.blue_button:
                    mChosenTheme = ThemePreference.blue;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawableSelected);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.sky_button:
                    mChosenTheme = ThemePreference.sky;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawableSelected);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.seagreen_button:
                    mChosenTheme = ThemePreference.seagreen;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawableSelected);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.cyan_button:
                    mChosenTheme = ThemePreference.cyan;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawableSelected);
                    pinkButton.setImageDrawable(pinkDrawable);

                    break;

                case R.id.pink_button:
                    mChosenTheme = ThemePreference.pink;

                    original_greenButton.setImageDrawable(original_greenDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    purpleButton.setImageDrawable(purpleDrawable);
                    navyButton.setImageDrawable(navyDrawable);
                    blueButton.setImageDrawable(blueDrawable);
                    skyButton.setImageDrawable(skyDrawable);
                    seagreenButton.setImageDrawable(seagreenDrawable);
                    cyanButton.setImageDrawable(cyanDrawable);
                    pinkButton.setImageDrawable(pinkDrawableSelected);

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
        original_greenButton = (ImageView) view.findViewById(R.id.original_green_button);
        redButton = (ImageView) view.findViewById(R.id.red_button);
        orangeButton = (ImageView) view.findViewById(R.id.orange_button);
        purpleButton = (ImageView) view.findViewById(R.id.purple_button);
        navyButton = (ImageView) view.findViewById(R.id.navy_button);
        blueButton = (ImageView) view.findViewById(R.id.blue_button);
        skyButton = (ImageView) view.findViewById(R.id.sky_button);
        seagreenButton = (ImageView) view.findViewById(R.id.seagreen_button);
        cyanButton = (ImageView) view.findViewById(R.id.cyan_button);
        pinkButton = (ImageView) view.findViewById(R.id.pink_button);


        original_greenButton.setOnClickListener(mOnClickListener);
        redButton.setOnClickListener(mOnClickListener);
        orangeButton.setOnClickListener(mOnClickListener);
        purpleButton.setOnClickListener(mOnClickListener);
        navyButton.setOnClickListener(mOnClickListener);
        blueButton.setOnClickListener(mOnClickListener);
        skyButton.setOnClickListener(mOnClickListener);
        seagreenButton.setOnClickListener(mOnClickListener);
        cyanButton.setOnClickListener(mOnClickListener);
        pinkButton.setOnClickListener(mOnClickListener);



        loadDrawables(view.getContext());
        initButtons();
    }

    private void initButtons() {
        switch (mChosenTheme) {
            case ThemePreference.original_green:
                original_greenButton.setImageDrawable(original_greenDrawableSelected);
                break;
            case ThemePreference.red:
                redButton.setImageDrawable(redDrawableSelected);
                break;
            case ThemePreference.orange:
                orangeButton.setImageDrawable(orangeDrawableSelected);
                break;
            case ThemePreference.purple:
                purpleButton.setImageDrawable(purpleDrawableSelected);
                break;
            case ThemePreference.navy:
                navyButton.setImageDrawable(navyDrawableSelected);
                break;
            case ThemePreference.blue:
                blueButton.setImageDrawable(blueDrawableSelected);
                break;
            case ThemePreference.sky:
                skyButton.setImageDrawable(skyDrawableSelected);
                break;
            case ThemePreference.seagreen:
                seagreenButton.setImageDrawable(seagreenDrawableSelected);
                break;
            case ThemePreference.cyan:
                cyanButton.setImageDrawable(cyanDrawableSelected);
                break;
            case ThemePreference.pink:
                pinkButton.setImageDrawable(pinkDrawableSelected);
                break;
        }
    }


    private void loadDrawables(Context context) {

        original_greenDrawable = ContextCompat.getDrawable(context, R.drawable.theme_original_green);
        original_greenDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_original_green_selected);

        redDrawable = ContextCompat.getDrawable(context, R.drawable.theme_red);
        redDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_red_selected);

        orangeDrawable = ContextCompat.getDrawable(context, R.drawable.theme_orange);
        orangeDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_orange_selected);

        purpleDrawable = ContextCompat.getDrawable(context, R.drawable.theme_purple);
        purpleDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_purple_selected);

        navyDrawable = ContextCompat.getDrawable(context, R.drawable.theme_navy);
        navyDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_navy_selected);

        blueDrawable = ContextCompat.getDrawable(context, R.drawable.theme_blue);
        blueDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_blue_selected);

        skyDrawable = ContextCompat.getDrawable(context, R.drawable.theme_sky);
        skyDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_sky_selected);

        seagreenDrawable = ContextCompat.getDrawable(context, R.drawable.theme_seagreen);
        seagreenDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_seagreen_selected);

        cyanDrawable = ContextCompat.getDrawable(context, R.drawable.theme_cyan);
        cyanDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_cyan_selected);

        pinkDrawable = ContextCompat.getDrawable(context, R.drawable.theme_pink);
        pinkDrawableSelected = ContextCompat.getDrawable(context, R.drawable.theme_pink_selected);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mPreference.save(positiveResult, mChosenTheme);
    }


}
