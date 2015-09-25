package com.andryr.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SwitchCompat;
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

public class EqualizerActivity extends BaseActivity {

    private PlaybackService mService;

    private boolean mIsBound;

    private SwitchCompat mSwitchButton;
    private boolean mSwitchBound = false;

    private Spinner mSpinner;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mService = ((PlaybackService.PlaybackBinder) service).getService();

            init();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        mSwitchBound = false;

        startService(new Intent(this, PlaybackService.class));

        Intent intent = new Intent(this, PlaybackService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        startService(intent);

        // Set to "bound"
        mIsBound = true;

    }


    @Override
    public void onPause() {
        super.onPause();

        if (mIsBound) {
            mService.saveAudioEffectsPreferences();
            unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    private void bindSwitchToEqualizer() {
        if (!mSwitchBound && mService != null && mSwitchButton != null) {

            mSwitchButton.setChecked(mService.areAudioEffectsEnabled());
            mSwitchButton
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            mService.setAudioEffectsEnabled(isChecked);

                        }
                    });
            mSwitchBound = true;
        }
    }

    private void init() {
        if (mService == null) {
            return;
        }

        bindSwitchToEqualizer();

        initBassBoost();

        initPresets();

        updateSliders();
    }

    private void initPresets() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                mService.getEqualizerPresets());

        mSpinner = (Spinner) findViewById(R.id.presets_spinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);

        mSpinner.setSelection(mService.getCurrentPreset());

        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (mService != null && position >= 1) {
                    mService.usePreset((short) (position - 1));
                    updateSliders();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void initBassBoost() {
        SeekBar bassBoost = (SeekBar) findViewById(R.id.bassboost_slider);
        bassBoost.setMax(PlaybackService.BASSBOOST_MAX_STRENGTH);
        bassBoost.setProgress(mService.getBassBoostStrength());
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
                    mService.setBassBoostStrength((short) seekBar.getProgress());
                }

            }
        });
    }

    private void updateSliders() {
        ViewGroup layout = (ViewGroup) findViewById(R.id.equalizer_layout);

        boolean empty = layout.getChildCount() == 0;

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
        short bands = mService.getNumberOfBands();

        for (short b = 0; b < bands; b++) {

            final short band = b;
            View v = null;
            if (empty) {
                v = getLayoutInflater().inflate(R.layout.equalizer_slider,
                        layout, false);
            } else {
                v = layout.getChildAt(b);
            }
            TextView freqTextView = (TextView) v.findViewById(R.id.frequency);
            final TextView levelTextView = (TextView) v
                    .findViewById(R.id.level);
            SeekBar seekBar = (SeekBar) v.findViewById(R.id.seek_bar);

            int freq = mService.getCenterFreq(band);
            if (freq < 1000 * 1000) {
                freqTextView.setText(freq / 1000 + "Hz");
            } else {
                freqTextView.setText(freq / (1000 * 1000) + "kHz");

            }

            final short[] range = mService.getBandLevelRange();
            seekBar.setMax(range[1] - range[0]);
            short level = mService.getBandLevel(band);
            seekBar.setProgress(level - range[0]);

            if (level / 100 == 0) {
                levelTextView.setText("0dB");
            } else {
                levelTextView.setText((level > 0 ? "+" : "-") + level / 100 + "dB");
            }

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
                        short level = (short) (seekBar.getProgress() + range[0]);
                        mService.setBandLevel(band, level);
                        levelTextView.setText(level / 100 + "dB");
                        mSpinner.setSelection(0);
                    }

                }
            });

            if (empty) {
                layout.addView(v, b, lp);
            }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
