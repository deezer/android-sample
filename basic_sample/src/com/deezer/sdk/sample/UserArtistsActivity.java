package com.deezer.sdk.sample;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

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

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.OAuthException;
import com.deezer.sdk.player.ArtistRadioPlayer;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;


/**
 * Activity displaying a list of a user's favorite artists
 * 
 * @author Deezer
 * 
 */
public class UserArtistsActivity extends PlayerActivity implements RadioPlayerListener {
    
    /** The list of artists of displayed by this activity. */
    private List<Artist> mArtistsList = new ArrayList<Artist>();
    
    /** the Artists list adapter */
    private ArrayAdapter<Artist> mArtistsAdapter;
    
    private ArtistRadioPlayer mArtistPlayer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        // Setup the UI
        setContentView(R.layout.activity_tracklists);
        setupArtistsList();
        setupPlayerUI();
        
        
        //build the player
        createPlayer();
        
        // fetch artists list 
        getUserArtists();
    }
    
    /**
     * Sets up the player UI (mostly remove unnecessary buttons)
     */
    private void setupPlayerUI() {
        // for now hide the player
        setPlayerVisible(false);
        
        // disable unnecesary buttons
        setButtonEnabled(mButtonPlayerSeekBackward, false);
        setButtonEnabled(mButtonPlayerSeekForward, false);
        setButtonEnabled(mButtonPlayerSkipBackward, false);
        setButtonEnabled(mButtonPlayerRepeat, false);
    }
    
    
    /**
     * Setup the List UI
     */
    private void setupArtistsList() {
        mArtistsAdapter = new ArrayAdapter<Artist>(this,
                android.R.layout.simple_list_item_1, mArtistsList) {
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Artist artist = getItem(position);
                
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(android.R.layout.simple_list_item_1, null);
                }
                
                
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(artist.getName());
                
                return view;
            }
        };
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setAdapter(mArtistsAdapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistsList.get(position);
                mArtistPlayer.playArtistRadio(artist.getId());
                setPlayerVisible(true);
            }
        });
    }
    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mArtistPlayer = new ArtistRadioPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            mArtistPlayer.addPlayerListener(this);
            setAttachedPlayer(mArtistPlayer);
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
     * Search for all artists splitted by genre
     */
    private void getUserArtists() {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserArtists();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect,
                new JsonRequestListener() {
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(Object result, Object requestId) {
                        
                        mArtistsList.clear();
                        
                        try {
                            mArtistsList.addAll((List<Artist>) result);
                        }
                        catch (ClassCastException e) {
                            handleError(e);
                        }
                        
                        if (mArtistsList.isEmpty()) {
                            Toast.makeText(UserArtistsActivity.this, getResources()
                                    .getString(R.string.no_results), Toast.LENGTH_LONG).show();
                        }
                        
                        mArtistsAdapter.notifyDataSetChanged();
                    }
                    
                    @Override
                    public void onOAuthException(OAuthException exception, Object requestId) {
                        handleError(exception);
                    }
                    
                    @Override
                    public void onMalformedURLException(MalformedURLException exception,
                            Object requestId) {
                        handleError(exception);
                    }
                    
                    @Override
                    public void onIOException(IOException exception, Object requestId) {
                        handleError(exception);
                    }
                    
                    @Override
                    public void onDeezerError(DeezerError exception, Object requestId) {
                        handleError(exception);
                    }
                    
                    @Override
                    public void onJSONParseException(JSONException exception, Object requestId) {
                        handleError(exception);
                    }
                    
                    
                });
        task.execute(request);
    }
    
    @Override
    protected void onSkipToNextTrack() {
        mArtistPlayer.skipToNextTrack();
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Player listener
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onTooManySkipsException() {
        Toast.makeText(this, R.string.deezer_too_many_skips,
                Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onPlayTrack(Track track) {
        displayTrack(track);
    }
    
    @Override
    public void onTrackEnded(Track track) {
    }
    
    @Override
    public void onAllTracksEnded() {
    }
    
    
    @Override
    public void onRequestDeezerError(DeezerError e, Object requestId) {
        handleError(e);
    }
    
    @Override
    public void onRequestIOException(IOException e, Object requestId) {
        handleError(e);
    }
    
    @Override
    public void onRequestJSONException(JSONException e, Object requestId) {
        handleError(e);
    }
    
    @Override
    public void onRequestMalformedURLException(MalformedURLException e, Object requestId) {
        handleError(e);
    }
    
    @Override
    public void onRequestOAuthException(OAuthException e, Object requestId) {
        handleError(e);
    }
}
