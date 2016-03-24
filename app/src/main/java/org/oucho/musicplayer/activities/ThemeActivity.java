package org.oucho.musicplayer.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.utils.ThemeHelper;

public class ThemeActivity extends BaseActivity implements
        View.OnClickListener {

    private final int[] bouton_ID = {

            R.id.original_green_button,
            R.id.red_button,
            R.id.orange_button,
            R.id.purple_button,
            R.id.navy_button,
            R.id.blue_button,
            R.id.sky_button,
            R.id.seagreen_button,
            R.id.cyan_button,
            R.id.pink_button
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.theme_preference_dialog);

        for (int ID : bouton_ID) {
            this.findViewById(ID).setOnClickListener(this);
        }

        String couleur = BaseActivity.getColor(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#" + couleur + "'>Thème</font>"));
        actionBar.setElevation(0);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
        upArrow.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.ImageControlColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

    }

    final String fichier_préférence = "org.oucho.musicplayer_preferences";
    SharedPreferences préférences = null;


    @SuppressLint("CommitPrefEdits")
    @Override
    public void onClick(View v) {

        préférences = getSharedPreferences(fichier_préférence, MODE_PRIVATE);

        Editor editor = préférences.edit();


        switch (v.getId()) {
            case R.id.original_green_button:
                editor.putInt("pref_theme", 1);
                editor.commit();
                recreate();
                break;

            case R.id.red_button:
                editor.putInt("pref_theme", 2);
                editor.commit();
                recreate();
                break;

            case R.id.orange_button:
                editor.putInt("pref_theme", 3);
                editor.commit();
                recreate();
                break;

            case R.id.purple_button:
                editor.putInt("pref_theme", 4);
                editor.commit();
                recreate();
                break;

            case R.id.navy_button:
                editor.putInt("pref_theme", 5);
                editor.commit();
                recreate();
                break;

            case R.id.blue_button:
                editor.putInt("pref_theme", 6);
                editor.commit();
                recreate();
                break;

            case R.id.sky_button:
                editor.putInt("pref_theme", 7);
                editor.commit();
                recreate();
                break;

            case R.id.seagreen_button:
                editor.putInt("pref_theme", 8);
                editor.commit();
                recreate();
                break;

            case R.id.cyan_button:
                editor.putInt("pref_theme", 9);
                editor.commit();
                recreate();
                break;

            case R.id.pink_button:
                editor.putInt("pref_theme", 10);
                editor.commit();
                recreate();
                break;
        }
    }

}
