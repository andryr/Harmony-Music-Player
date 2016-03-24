package org.oucho.musicplayer.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import org.oucho.musicplayer.R;


public class ThemePreference extends DialogPreference {

    public static final int original_green = 1;
    public static final int red = 2;
    public static final int orange = 3;
    public static final int purple = 4;
    public static final int navy = 5;
    public static final int blue = 6;
    public static final int sky = 7;
    public static final int seagreen = 8;
    public static final int cyan = 9;
    public static final int pink = 10;



    public static final int DEFAULT_THEME = original_green;


    private int mChosenTheme;

    public ThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.theme_preference_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);


    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mChosenTheme = this.getPersistedInt(original_green);


        } else {
            mChosenTheme = (Integer) defaultValue;
            persistInt(mChosenTheme);

        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, original_green);
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

        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        mChosenTheme = myState.value;
    }

    public int getValue() {
        return mChosenTheme;
    }


    public void save(boolean positiveResult, int chosenTheme) {
        if (positiveResult) {
            mChosenTheme = chosenTheme;
            final boolean wasBlocking = shouldDisableDependents();

            persistInt(mChosenTheme);

            final boolean isBlocking = shouldDisableDependents();
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking);
            }
        }
    }

    private static class SavedState extends BaseSavedState {
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
    }


}
