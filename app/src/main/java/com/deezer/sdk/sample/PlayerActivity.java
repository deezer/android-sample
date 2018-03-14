package com.deezer.sdk.sample;

import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.PlayerWrapper.RepeatMode;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;



public class PlayerActivity extends BaseActivity {
    
    
    
    private PlayerHandler mPlayerHandler = new PlayerHandler();
    private OnClickHandler mOnClickHandler = new OnClickHandler();
    
    protected ImageButton mButtonPlayerStop;
    protected ImageButton mButtonPlayerPause;
    protected ImageButton mButtonPlayerSkipForward;
    protected ImageButton mButtonPlayerSkipBackward;
    protected ImageButton mButtonPlayerSeekBackward;
    protected ImageButton mButtonPlayerSeekForward;
    
    protected ImageButton mButtonPlayerRepeat;
    
    
    private SeekBar mSeekBar;
    private boolean mIsUserSeeking = false;
    private TextView mTextTime;
    private TextView mTextLength;
    
    private TextView mTextArtist;
    private TextView mTextTrack;
    
    private PlayerWrapper mPlayer;
    
    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        
        
        mButtonPlayerPause = (ImageButton) findViewById(R.id.button_pause);
        mButtonPlayerStop = (ImageButton) findViewById(R.id.button_stop);
        mButtonPlayerSkipForward = (ImageButton) findViewById(R.id.button_skip_forward);
        mButtonPlayerSkipBackward = (ImageButton) findViewById(R.id.button_skip_backward);
        mButtonPlayerSeekForward = (ImageButton) findViewById(R.id.button_seek_forward);
        mButtonPlayerSeekBackward = (ImageButton) findViewById(R.id.button_seek_backward);
        mButtonPlayerRepeat = (ImageButton) findViewById(R.id.button_repeat);
        
        
        mSeekBar = (SeekBar) findViewById(R.id.seek_progress);
        mTextTime = (TextView) findViewById(R.id.text_time);
        mTextLength = (TextView) findViewById(R.id.text_length);
        
        mTextArtist = (TextView) findViewById(R.id.text_artist);
        mTextTrack = (TextView) findViewById(R.id.text_track);
        
        
        mButtonPlayerPause.setOnClickListener(mOnClickHandler);
        mButtonPlayerStop.setOnClickListener(mOnClickHandler);
        mButtonPlayerSkipForward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSkipBackward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSeekForward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSeekBackward.setOnClickListener(mOnClickHandler);
        mButtonPlayerRepeat.setOnClickListener(mOnClickHandler);
    }
    
    
    @Override
    protected void onDestroy() {
        doDestroyPlayer();
        super.onDestroy();
    }
    
    /**
     * Will destroy player. Subclasses can override this hook.
     */
    protected void doDestroyPlayer() {
        
        if (mPlayer == null) {
            // No player, ignore
            return;
        }
        
        if (mPlayer.getPlayerState() == PlayerState.RELEASED) {
            // already released, ignore
            return;
        }
        
        // first, stop the player if it is not 
        if (mPlayer.getPlayerState() != PlayerState.STOPPED) {
            mPlayer.stop();
        }
        
        // then release it 
        mPlayer.release();
    }
    
    protected void setAttachedPlayer(final PlayerWrapper player) {
        mPlayer = player;
        player.addOnBufferErrorListener(mPlayerHandler);
        player.addOnBufferStateChangeListener(mPlayerHandler);
        player.addOnBufferProgressListener(mPlayerHandler);
        
        player.addOnPlayerErrorListener(mPlayerHandler);
        player.addOnPlayerStateChangeListener(mPlayerHandler);
        player.addOnPlayerProgressListener(mPlayerHandler);
        
        if (mPlayer.isAllowedToSeek()) {
            mSeekBar.setEnabled(true);
        }
    }
    
    protected void displayTrack(final Track track) {
        // artist name 
        if ((track.getArtist() == null) || (track.getArtist().getName() == null)) {
            mTextArtist.setVisibility(View.GONE);
        } else {
            mTextArtist.setVisibility(View.VISIBLE);
            mTextArtist.setText(track.getArtist().getName());
        }
        
        // track name 
        if (track.getTitle() == null) {
            mTextTrack.setVisibility(View.GONE);
        } else {
            mTextTrack.setVisibility(View.VISIBLE);
            mTextTrack.setText(track.getTitle());
        }
    }
    
    /**
     * @param visible
     *            if the player UI should be visible
     */
    protected void setPlayerVisible(final boolean visible) {
        if (visible) {
            findViewById(R.id.player).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.player).setVisibility(View.GONE);
        }
    }
    
    protected void setButtonEnabled(final View button, final boolean enabled) {
        if (enabled) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }
        button.setEnabled(enabled);
    }
    
    /**
     * Displays the current progression of the playback
     * 
     * @param state
     *            the current player's state
     */
    public void showPlayerProgress(final long timePosition) {
        if (!mIsUserSeeking) {
            mSeekBar.setProgress((int) timePosition / 1000);
            String text = formatTime(timePosition);
            mTextTime.setText(text);
        }
        
        mSeekBar.setEnabled(false);
    }
    
    /**
     * Displays the current state of the player
     * 
     * @param state
     *            the current player's state
     */
    public void showPlayerState(final PlayerState state) {
        mSeekBar.setEnabled(true);
        mButtonPlayerPause.setEnabled(true);
        mButtonPlayerStop.setEnabled(true);
        
        switch (state) {
            case STARTED:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            case INITIALIZING:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            case READY:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_play);
                showPlayerProgress(0);
                break;
            case PLAYING:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_pause);
                break;
            case PAUSED:
            case PLAYBACK_COMPLETED:
                mButtonPlayerPause
                        .setEnabled(true /*player.getPosition() != player.getTrackDuration()*/);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_play);
                break;
            
            case WAITING_FOR_DATA:
                mButtonPlayerPause.setEnabled(false);
                break;
            
            case STOPPED:
                mSeekBar.setEnabled(false);
                showPlayerProgress(0);
                showBufferProgress(0);
                mButtonPlayerPause.setImageResource(R.drawable.ic_action_play);
                mButtonPlayerStop.setEnabled(false);
                break;
            case RELEASED:
                break;
            default:
                break;
        }
    }
    
    
    /**
     * displays the current progression of the buffer
     * 
     * @param position
     */
    public void showBufferProgress(final int position) {
        synchronized (this) {
            if (mPlayer != null) {
                if (position > 0) {
                    showTrackDuration(mPlayer.getTrackDuration());
                }
                long progress = (position * mPlayer.getTrackDuration()) / 100;
                mSeekBar.setSecondaryProgress((int) progress / 1000);
            }
        }
    }
    
    /**
     * 
     * @param trackLength
     */
    public void showTrackDuration(final long trackLength) {
        String text = formatTime(trackLength);
        mTextLength.setText(text);
        mSeekBar.setMax((int) trackLength / 1000);
    }
    
    
    /**
     * Formats a time.
     * 
     * @param time
     *            the time (in seconds)
     * @return the formatted time.
     */
    private static String formatTime(long time) {
        time /= 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time;
        StringBuilder builder = new StringBuilder(8);
        doubleDigit(builder, seconds);
        builder.insert(0, ':');
        if (hours == 0) {
            builder.insert(0, minutes);
        } else {
            doubleDigit(builder, minutes);
            builder.insert(0, ':');
            builder.insert(0, hours);
        }
        return builder.toString();
    }
    
    /**
     * Ensure double decimal representation of numbers.
     * 
     * @param builder
     *            a builder where a number is gonna be inserted at beginning.
     * @param value
     *            the number value. If below 10 then a leading 0 is inserted.
     */
    private static void doubleDigit(final StringBuilder builder, final long value) {
        builder.insert(0, value);
        if (value < 10) {
            builder.insert(0, '0');
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Click handler 
    //////////////////////////////////////////////////////////////////////////////////////
    
    private class OnClickHandler implements View.OnClickListener {
        
        @Override
        public void onClick(final View v) {
            if (v == mButtonPlayerPause) {
                if (mPlayer.getPlayerState() == PlayerState.PLAYING) {
                    mPlayer.pause();
                } else {
                    mPlayer.play();
                }
            } else if (v == mButtonPlayerStop) {
                mPlayer.stop();
                //setPlayerVisible(false);
            } else if (v == mButtonPlayerSkipForward) {
                onSkipToNextTrack();
            } else if (v == mButtonPlayerSkipBackward) {
                onSkipToPreviousTrack();
            } else if (v == mButtonPlayerSeekBackward) {
                try {
                    mPlayer.seek(mPlayer.getPosition() - (10 * 1000));
                }
                catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerSeekForward) {
                try {
                    mPlayer.seek(mPlayer.getPosition() + (10 * 1000));
                }
                catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerRepeat) {
                switchRepeatMode();
            }
        }
    }
    
    protected void onSkipToNextTrack() {
        
    }
    
    protected void onSkipToPreviousTrack() {
        
    }
    
    protected void switchRepeatMode() {
        RepeatMode current = mPlayer.getRepeatMode();
        RepeatMode next;
        String toast;
        
        switch (current) {
            case NONE:
                next = RepeatMode.ONE;
                toast = "Repeat mode set to : Repeat One";
                break;
            case ONE:
                next = RepeatMode.ALL;
                toast = "Repeat mode set to : Repeat All";
                break;
            case ALL:
            default:
                next = RepeatMode.NONE;
                toast = "Repeat mode set to : No Repeat";
                break;
        }
        
        mPlayer.setRepeatMode(next);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Player Handler
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Handler for messages sent by the player and buffer
     */
    private class PlayerHandler
            implements
            OnPlayerProgressListener,
            OnBufferProgressListener,
            OnPlayerStateChangeListener,
            OnPlayerErrorListener,
            OnBufferStateChangeListener,
            OnBufferErrorListener {
        
        @Override
        public void onBufferError(final Exception ex, final double percent) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    handleError(ex);
                }
            });
        }
        
        @Override
        public void onBufferStateChange(final BufferState state, final double percent) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showBufferProgress((int) Math.round(percent));
                }
            });
        }
        
        @Override
        public void onPlayerError(final Exception ex, final long timePosition) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    handleError(ex);
                    if (ex instanceof NotAllowedToPlayThatSongException) {
                        mPlayer.skipToNextTrack();
                    } else if (ex instanceof StreamLimitationException) {
                        // Do nothing , 
                    } else {
                        finish();
                    }
                }
            });
        }
        
        @Override
        public void onPlayerStateChange(final PlayerState state, final long timePosition) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showPlayerState(state);
                    showPlayerProgress(timePosition);
                }
            });
        }
        
        @Override
        public void onBufferProgress(final double percent) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showBufferProgress((int) Math.round(percent));
                }
            });
        }
        
        @Override
        public void onPlayerProgress(final long timePosition) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    showPlayerProgress(timePosition);
                }
            });
        }
    }
}
