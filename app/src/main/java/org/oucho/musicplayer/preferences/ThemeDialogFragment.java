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
    private ImageView redButton;
    private ImageView orangeButton;


    private Drawable original_greenDrawable;
    private Drawable original_greenSelectedDrawable;

    private Drawable redDrawable;
    private Drawable redSelectedDrawable;

    private Drawable orangeDrawable;
    private Drawable orangeSelectedDrawable;

    private ThemePreference mPreference;

    private int mChosenTheme;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.original_green_button:
                    mChosenTheme = ThemePreference.original_green;
                    original_greenButton.setImageDrawable(original_greenSelectedDrawable);
                    redButton.setImageDrawable(redDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    break;
                case R.id.red_button:
                    mChosenTheme = ThemePreference.red;
                    redButton.setImageDrawable(redSelectedDrawable);
                    orangeButton.setImageDrawable(orangeDrawable);
                    original_greenButton.setImageDrawable(original_greenDrawable);
                    break;
                case R.id.orange_button:
                    mChosenTheme = ThemePreference.orange;
                    orangeButton.setImageDrawable(orangeSelectedDrawable);
                    redButton.setImageDrawable(redDrawable);
                    original_greenButton.setImageDrawable(original_greenDrawable);
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

        original_greenButton.setOnClickListener(mOnClickListener);
        redButton.setOnClickListener(mOnClickListener);
        orangeButton.setOnClickListener(mOnClickListener);


        loadDrawables(view.getContext());
        initButtons();
    }

    private void initButtons() {
        switch (mChosenTheme) {
            case ThemePreference.original_green:
                original_greenButton.setImageDrawable(original_greenSelectedDrawable);
                break;
            case ThemePreference.red:
                redButton.setImageDrawable(redSelectedDrawable);
                break;
            case ThemePreference.orange:
                orangeButton.setImageDrawable(orangeSelectedDrawable);
                break;

        }

    }


    private void loadDrawables(Context context) {

        original_greenDrawable = ContextCompat.getDrawable(context, R.drawable.theme_original_green_button_normal);
        original_greenSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.theme_original_green_button_selected);

        redDrawable = ContextCompat.getDrawable(context, R.drawable.theme_red_button_normal);
        redSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.theme_red_button_selected);

        orangeDrawable = ContextCompat.getDrawable(context, R.drawable.theme_orange_button_normal);
        orangeSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.theme_orange_button_selected);

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mPreference.save(positiveResult, mChosenTheme);
    }


}
