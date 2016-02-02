/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.andryr.musicplayer.R;


public class ThemePreference extends DialogPreference {


    public static final int DARK_BLUE_GREY_THEME = 0;
    public static final int BLUE_GREY_THEME = 1;
    public static final int BLUE_THEME = 2;


    public static final int DEFAULT_THEME = DARK_BLUE_GREY_THEME;


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
            mChosenTheme = this.getPersistedInt(DARK_BLUE_GREY_THEME);


        } else {
            mChosenTheme = (Integer) defaultValue;
            persistInt(mChosenTheme);

        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DARK_BLUE_GREY_THEME);
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
