package com.andryr.musicplayer.audiofx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AudioEffectsReceiver extends BroadcastReceiver {

    public static final String EXTRA_AUDIO_SESSION_ID = "com.andryr.musicplayer.EXTRA_AUDIO_SESSION_ID";

    public static final String ACTION_OPEN_AUDIO_EFFECT_SESSION = "com.andryr.musicplayer.OPEN_AUDIO_EFFECT_SESSION";
    public static final String ACTION_CLOSE_AUDIO_EFFECT_SESSION = "com.andryr.musicplayer.CLOSE_AUDIO_EFFECT_SESSION";

    public AudioEffectsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int audioSessionId = intent.getIntExtra(EXTRA_AUDIO_SESSION_ID, 0);
        if(ACTION_OPEN_AUDIO_EFFECT_SESSION.equals(action))
        {
            AudioEffects.openAudioEffectSession(context, audioSessionId);
        }
        else if(ACTION_CLOSE_AUDIO_EFFECT_SESSION.equals(action))
        {
            AudioEffects.closeAudioEffectSession();
        }

    }
}
