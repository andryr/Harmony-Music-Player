package com.andryr.musicplayer.fragments.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.andryr.musicplayer.R;


public class ThemeDialog extends DialogPreference {




    public static final int ORANGE_THEME = 1;
    public static final int BLUE_THEME = 2;



    private ImageView mBlueButton;
    private ImageView mOrangeButton;


    private Drawable mOrangeDrawable;
    private Drawable mOrangeSelectedDrawable;

    private Drawable mBlueDrawable;
    private Drawable mBlueSelectedDrawable;


    private int mChosenTheme;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.orange_button:
                    mChosenTheme = ORANGE_THEME;
                    mOrangeButton.setImageDrawable(mOrangeSelectedDrawable);
                    mBlueButton.setImageDrawable(mBlueDrawable);
                    break;
                case R.id.blue_button:
                    mChosenTheme = BLUE_THEME;
                    mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                    mOrangeButton.setImageDrawable(mOrangeDrawable);
                    break;
            }

        }
    };

    public ThemeDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.theme_preference_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

        loadDrawables(context);

    }

    private void initButtons() {
        Log.d("theme"," ee1 "+mChosenTheme);
        switch (mChosenTheme) {
            case ORANGE_THEME:
                mOrangeButton.setImageDrawable(mOrangeSelectedDrawable);
                break;
            case BLUE_THEME:
                mBlueButton.setImageDrawable(mBlueSelectedDrawable);
                break;

        }

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mBlueButton = (ImageView) view.findViewById(R.id.blue_button);
        mOrangeButton = (ImageView) view.findViewById(R.id.orange_button);

        mBlueButton.setOnClickListener(mOnClickListener);
        mOrangeButton.setOnClickListener(mOnClickListener);

        initButtons();

    }

    private void loadDrawables(Context context) {
        Resources res = context.getResources();

        mOrangeDrawable = res.getDrawable(R.drawable.blue_grey_theme_button_normal);
        mOrangeSelectedDrawable = res.getDrawable(R.drawable.blue_grey__theme_button_selected);

        mBlueDrawable = res.getDrawable(R.drawable.blue_theme_button_normal);
        mBlueSelectedDrawable = res.getDrawable(R.drawable.blue_theme_button_selected);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            final boolean wasBlocking = shouldDisableDependents();

            persistInt(mChosenTheme);

            final boolean isBlocking = shouldDisableDependents();
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking);
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mChosenTheme = this.getPersistedInt(BLUE_THEME);


        } else {
            mChosenTheme = (Integer) defaultValue;
            persistInt(mChosenTheme);

        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, BLUE_THEME);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.value = mChosenTheme;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        mChosenTheme = myState.value;
        initButtons();
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


}
