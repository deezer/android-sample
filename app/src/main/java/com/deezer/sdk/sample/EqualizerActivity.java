package com.deezer.sdk.sample;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.RadioPlayer.RadioType;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import java.util.Arrays;


public class EqualizerActivity extends PlayerActivity
        implements
        RadioPlayerListener,
        OnSeekBarChangeListener {
    
    private static final long RADIO_SOUNDTRACKS = 30701L;
    private RadioPlayer mRadioPlayer;
    private Equalizer mEqualizer;
    
    private SeekBar[] mSeekBars;
    private short[] mRange;
    
    private static final int MIN_SEEK = 0;
    private static final int MAX_SEEK = 100;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        // setup UI
        setContentView(R.layout.activity_equalizer);
        setupPlayerUI();
        setupEqualizerUI();
        
        //build the player
        createPlayer();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mRadioPlayer.playRadio(RadioType.RADIO, RADIO_SOUNDTRACKS);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mRadioPlayer.stop();
//        stopVisualizer();
    }
    
    /**
     * Sets up the player UI (mostly remove unnecessary buttons)
     */
    private void setupPlayerUI() {
        // for now hide the player
        setPlayerVisible(true);
        
        // disable unnecesary buttons
        setButtonEnabled(mButtonPlayerSeekBackward, false);
        setButtonEnabled(mButtonPlayerSeekForward, false);
        setButtonEnabled(mButtonPlayerSkipBackward, false);
        setButtonEnabled(mButtonPlayerSkipForward, false);
        setButtonEnabled(mButtonPlayerStop, false);
        setButtonEnabled(mButtonPlayerPause, false);
        
        setButtonEnabled(mButtonPlayerRepeat, false);
    }
    
    
    private void setupEqualizerUI() {
        
        ViewGroup parent = (ViewGroup) findViewById(R.id.equalizer_group);
        
        mSeekBars = new SeekBar[parent.getChildCount()];
        for (int i = 0; i < mSeekBars.length; ++i) {
            mSeekBars[i] = (SeekBar) parent.getChildAt(i);
            mSeekBars[i].setMax(MAX_SEEK);
        }
    }
    
    /**
     * Creates the Radio Player
     */
    private void createPlayer() {
        try {
            mRadioPlayer = new RadioPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            mRadioPlayer.addPlayerListener(this);
            setAttachedPlayer(mRadioPlayer);
        }
        catch (DeezerError e) {
            handleError(e);
        }
        catch (TooManyPlayersExceptions e) {
            handleError(e);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // OnSeekBarChangeListener Implementation
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }
    
    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        
        
    }
    
    
    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        
        for (int i = 0; i < mSeekBars.length; i++) {
            if (mSeekBars[i] == seekBar) {
                float percent = ((float) seekBar.getProgress()) / MAX_SEEK;
                short newLevel = (short) (mRange[0] + (percent * (mRange[1] - mRange[0])));
                
                Log.i("Equalizer", "Level " + i + " set to " + newLevel + " milliBels");
                mEqualizer.setBandLevel((short) i, newLevel);
                break;
            }
        }
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Radio Player Callbacks
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAllTracksEnded() {
    }

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {
        if(playableEntity.getType()=="track")
            displayTrack((Track)playableEntity);

        mEqualizer = new Equalizer(0, mRadioPlayer.getAudioSessionId());
        mEqualizer.setEnabled(true);

        int numBands = mEqualizer.getNumberOfBands();
        Log.i("Equalizer", "Number of bands : " + numBands);

        mRange = mEqualizer.getBandLevelRange();
        Log.i("Equalizer", "Level range : " + Arrays.toString(mRange) + " milliBels");

        for (int i = 0; i < mSeekBars.length; ++i) {

            if (i < numBands) {
                mSeekBars[i].setVisibility(View.VISIBLE);

                int percent = ((mEqualizer.getBandLevel((short) i) - mRange[0]) * MAX_SEEK)
                        / (mRange[1] - mRange[0]);
                mSeekBars[i].setOnSeekBarChangeListener(null);
                mSeekBars[i].setProgress(percent);
                mSeekBars[i].setOnSeekBarChangeListener(this);
            }
            else {
                mSeekBars[i].setVisibility(View.GONE);
                mSeekBars[i].setOnSeekBarChangeListener(null);
            }

        }
    }

    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {

    }


    @Override
    public void onRequestException(final Exception e, final Object requestId) {
        handleError(e);
    }
    
    @Override
    public void onTooManySkipsException() {
        Toast.makeText(this, R.string.deezer_too_many_skips,
                Toast.LENGTH_LONG).show();
    }
    
}
