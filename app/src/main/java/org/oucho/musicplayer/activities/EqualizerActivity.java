package org.oucho.musicplayer.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.audiofx.AudioEffects;
import org.oucho.musicplayer.utils.ThemeHelper;

public class EqualizerActivity extends BaseActivity {


    private SwitchCompat mSwitchButton;
    private boolean mSwitchBound;

    private Spinner mSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        String couleur = BaseActivity.getColor(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#" + couleur + "'>Egaliseur</font>"));
        actionBar.setElevation(0);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
        upArrow.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.ImageControlColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        mSwitchBound = false;
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        AudioEffects.savePrefs(this);
    }

    private void bindSwitchToEqualizer() {
        if (!mSwitchBound && mSwitchButton != null) {

            mSwitchButton.setChecked(AudioEffects.areAudioEffectsEnabled());
            mSwitchButton
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            AudioEffects.setAudioEffectsEnabled(isChecked);

                        }
                    });
            mSwitchBound = true;
        }
    }

    private void init() {


        bindSwitchToEqualizer();

        initBassBoost();

        initSeekBars();

        updateSeekBars();

        initPresets();
    }

    private void initPresets() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                AudioEffects.getEqualizerPresets(this));

        mSpinner = (Spinner) findViewById(R.id.presets_spinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);

        mSpinner.setSelection(AudioEffects.getCurrentPreset());

        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position >= 1) {
                    AudioEffects.usePreset((short) (position - 1));
                }
                updateSeekBars();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initBassBoost() {
        SeekBar bassBoost = (SeekBar) findViewById(R.id.bassboost_slider);
        bassBoost.setMax(AudioEffects.BASSBOOST_MAX_STRENGTH);
        bassBoost.setProgress(AudioEffects.getBassBoostStrength());
        bassBoost.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    AudioEffects.setBassBoostStrength((short) seekBar.getProgress());
                }
            }
        });
    }

    private void initSeekBars() {
            ViewGroup layout = (ViewGroup) findViewById(R.id.equalizer_layout);

            final short[] range = AudioEffects.getBandLevelRange();

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            short bands = AudioEffects.getNumberOfBands();

            for (short band = 0; band < bands; band++) {

                View v = getLayoutInflater().inflate(R.layout.equalizer_slider,
                            layout, false);


                SeekBar seekBar = (SeekBar) v.findViewById(R.id.seek_bar);


                seekBar.setMax((range != null ? range[1] : 0) - range[0]);

                seekBar.setTag(band);

                final TextView levelTextView = (TextView) v
                        .findViewById(R.id.level);
                seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {

                        if (fromUser) {
                            short band = (Short) seekBar.getTag();
                            short level = (short) (seekBar.getProgress() + range[0]);
                            AudioEffects.setBandLevel(band, level);
                            levelTextView.setText((level > 0 ? "+" : "") + level / 100 + "dB");
                            mSpinner.setSelection(0);
                        }
                    }
                });

                layout.addView(v, band, lp);
            }
    }

    private void updateSeekBars() {
        ViewGroup layout = (ViewGroup) findViewById(R.id.equalizer_layout);

        final short[] range = AudioEffects.getBandLevelRange();

        short bands = AudioEffects.getNumberOfBands();

        for (short band = 0; band < bands; band++) {

            View v = layout.getChildAt(band);

            final TextView freqTextView = (TextView) v.findViewById(R.id.frequency);
            final TextView levelTextView = (TextView) v
                    .findViewById(R.id.level);
            final SeekBar seekBar = (SeekBar) v.findViewById(R.id.seek_bar);


            int freq = AudioEffects.getCenterFreq(band);
            if (freq < 1000 * 1000) {
                freqTextView.setText(freq / 1000 + "Hz");
            } else {
                freqTextView.setText(freq / (1000 * 1000) + "kHz");
            }


            short level = AudioEffects.getBandLevel(band);
            seekBar.setProgress(level - (range != null ? range[0] : 0));


            levelTextView.setText((level > 0 ? "+" : "") + level / 100 + "dB");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.equalizer, menu);
        MenuItem item = menu.findItem(R.id.action_switch);

        mSwitchButton = (SwitchCompat) MenuItemCompat.getActionView(item)
                .findViewById(R.id.switch_button);
        bindSwitchToEqualizer();
        return true;
    }

}
