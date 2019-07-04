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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.squareup.picasso.Picasso;


/**
 * Displays a list of deezer Radios and lets the user plays them
 * 
 * @author Deezer
 * 
 */
public class UserRadiosActivity extends PlayerActivity implements RadioPlayerListener {
    
    /** The list of radios displayed by this activity. */
    private List<Radio> mRadioList = new ArrayList<Radio>();
    
    /** The adapter of the main list view. */
    private ArrayAdapter<Radio> mRadioAdapter;
    
    /** the Radio Player */
    private RadioPlayer mRadioPlayer;
    
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        
        // setup UI
        setContentView(R.layout.activity_tracklists);
        setupPlayerUI();
        setupRadioList();
        
        //build the player
        createPlayer();
        
        // fetch radio list 
        searchAllRadioCategory();
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
    
    @Override
    protected void onSkipToNextTrack() {
        super.onSkipToNextTrack();
        
        mRadioPlayer.skipToNextTrack();
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
     * Setup the expandable list view
     */
    private void setupRadioList() {
        mRadioAdapter = new ArrayAdapter<Radio>(this,
                R.layout.item_title_cover, mRadioList) {
            
            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                Radio radio = getItem(position);
                
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.item_title_cover, null);
                }
                
                
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(radio.getTitle());
                
                ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
                Picasso.with(UserRadiosActivity.this).load(radio.getPictureUrl()).into(imageView);
                
                return view;
            }
        };
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setAdapter(mRadioAdapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                Radio radio = mRadioList.get(position);
                mRadioPlayer.playRadio(radio.getId());
                setPlayerVisible(true);
            }
        });
    }
    
    /**
     * Search for all radios splitted by genre
     */
    private void searchAllRadioCategory() {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserRadios();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect,
                new JsonRequestListener() {
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(final Object result, final Object requestId) {
                        
                        mRadioList.clear();
                        
                        try {
                            mRadioList.addAll((List<Radio>) result);
                        }
                        catch (ClassCastException e) {
                            handleError(e);
                        }
                        if (mRadioList.isEmpty()) {
                            Toast.makeText(UserRadiosActivity.this, getResources()
                                    .getString(R.string.no_results), Toast.LENGTH_LONG).show();
                        }
                        
                        mRadioAdapter.notifyDataSetChanged();
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
    // Radio Player Callbacks
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {
        displayTrack((Track) playableEntity);
    }
    
    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {
    }
    
    @Override
    public void onAllTracksEnded() {
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
