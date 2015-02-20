package com.deezer.sdk.sample;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.OAuthException;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;


/**
 * Activity displaying a list of a user's most played tracks
 * 
 * @author Deezer
 * 
 */
public class UserTopTracksActivity extends PlayerActivity implements PlayerWrapperListener {
    
    /** The list of tracks of displayed by this activity. */
    private List<Track> mTracksList = new ArrayList<Track>();
    
    /** the tracks list adapter */
    private ArrayAdapter<Track> mTracksAdapter;
    
    private TrackPlayer mTrackPlayer;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        // Setup the UI
        setContentView(R.layout.activity_tracklists);
        setupTracksList();
        setupPlayerUI();
        
        //build the player
        createPlayer();
        
        // fetch tracks list 
        getUserTracks();
    }
    
    /**
     * Sets up the player UI (mostly remove unnecessary buttons)
     */
    private void setupPlayerUI() {
        // for now hide the player
        setPlayerVisible(false);
        
        // disable unnecesary buttons
        setButtonEnabled(mButtonPlayerSkipBackward, false);
        setButtonEnabled(mButtonPlayerSkipForward, false);
    }
    
    /**
     * Setup the List UI
     */
    private void setupTracksList() {
        mTracksAdapter = new ArrayAdapter<Track>(this,
                android.R.layout.simple_list_item_1, mTracksList) {
            
            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                Track track = getItem(position);
                
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(android.R.layout.simple_list_item_1, null);
                }
                
                
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(track.getTitle());
                
                return view;
            }
        };
        
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setAdapter(mTracksAdapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                Track track = mTracksList.get(position);
                mTrackPlayer.playTrack(track.getId());
                setPlayerVisible(true);
            }
        });
    }
    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mTrackPlayer = new TrackPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            mTrackPlayer.addPlayerListener(this);
            setAttachedPlayer(mTrackPlayer);
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
    
    /**
     * Search for all tracks splitted by genre
     */
    private void getUserTracks() {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserCharts();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect,
                new JsonRequestListener() {
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(final Object result, final Object requestId) {
                        
                        mTracksList.clear();
                        
                        try {
                            mTracksList.addAll((List<Track>) result);
                        }
                        catch (ClassCastException e) {
                            handleError(e);
                        }
                        
                        if (mTracksList.isEmpty()) {
                            Toast.makeText(UserTopTracksActivity.this, getResources()
                                    .getString(R.string.no_results), Toast.LENGTH_LONG).show();
                        }
                        
                        mTracksAdapter.notifyDataSetChanged();
                    }
                    
                    @Override
                    public void onUnparsedResult(final String response, final Object requestId) {
                        handleError(new DeezerError("Unparsed reponse"));
                    }
                    
                    @Override
                    public void onException(final Exception exception,
                            final Object requestId) {
                        handleError(exception);
                    }
                    
                });
        task.execute(request);
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
