package com.deezer.sdk.sample;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.OAuthException;
import com.deezer.sdk.player.CustomTrackListPlayer;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;


public class UserCustomTrackListActivity extends PlayerActivity implements PlayerWrapperListener {
    
    private CustomTrackListPlayer mCustomPlayer;
    
    private final List<String> TRACK_IDS = new LinkedList<String>() {
        
        /** */
        private static final long serialVersionUID = 1L;
        {
            add("6658600");
            add("12580151");
            add("6722353");
            add("6722352");
            add("72250595");
        }
    };
    
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        // Setup the UI
        setContentView(R.layout.activity_custom);
        setPlayerVisible(true);
        setupPlayerUI();
        
        //build the player
        createPlayer();
        
        // Play the tracks list
        playTrackList();
        
    }
    /**
     * Sets up the player UI (mostly remove unnecessary buttons)
     */
    private void setupPlayerUI() {
        // disable unnecesary buttons
        setButtonEnabled(mButtonPlayerSeekBackward, true);
        setButtonEnabled(mButtonPlayerSeekForward, true);
        setButtonEnabled(mButtonPlayerSkipBackward, true);
        setButtonEnabled(mButtonPlayerRepeat, true);
    }
    
    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mCustomPlayer = new CustomTrackListPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            mCustomPlayer.addPlayerListener(this);
            setAttachedPlayer(mCustomPlayer);
        }
        catch (OAuthException e) {
            handleError(e);
        }
        catch (TooManyPlayersExceptions e) {
            handleError(e);
        }
        catch (DeezerError e) {
            handleError(e);
        }
    }
    
    
    @Override
    protected void onSkipToNextTrack() {
        mCustomPlayer.skipToNextTrack();
    }
    
    @Override
    protected void onSkipToPreviousTrack() {
        mCustomPlayer.skipToPreviousTrack();
    }
    
    /**
     * 
     */
    private void playTrackList() {
        mCustomPlayer.playTrackList(TRACK_IDS);
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Player listener
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onPlayTrack(final Track track) {
        displayTrack(track);
    }
    
    @Override
    public void onTrackEnded(final Track track) {
    }
    
    @Override
    public void onAllTracksEnded() {
    }
    
    @Override
    public void onRequestException(final Exception e, final Object requestId) {
        handleError(e);
    }
    
}
