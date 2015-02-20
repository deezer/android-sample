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

import com.deezer.sdk.model.AImageOwner.ImageSize;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.OAuthException;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.squareup.picasso.Picasso;


/**
 * Activity displaying a list of a user's favorite albums
 * 
 * @author Deezer
 * 
 */
public class UserAlbumsActivity extends PlayerActivity implements PlayerWrapperListener {
    
    /** The list of albums of displayed by this activity. */
    private List<Album> mAlbumsList = new ArrayList<Album>();
    
    /** the Albums list adapter */
    private ArrayAdapter<Album> mAlbumsAdapter;
    
    private AlbumPlayer mAlbumPlayer;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);
        
        // Setup the UI
        setContentView(R.layout.activity_tracklists);
        setupAlbumsList();
        setPlayerVisible(false);
        
        //build the player
        createPlayer();
        
        // fetch albums list 
        getUserAlbums();
    }
    
    /**
     * Setup the List UI
     */
    private void setupAlbumsList() {
        mAlbumsAdapter = new ArrayAdapter<Album>(this,
                R.layout.item_title_cover, mAlbumsList) {
            
            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                Album album = getItem(position);
                
                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.item_title_cover, null);
                }
                
                
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(album.getTitle());
                
                ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
                Picasso.with(UserAlbumsActivity.this).load(album.getImageUrl(ImageSize.small))
                        .into(imageView);
                
                return view;
            }
        };
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setAdapter(mAlbumsAdapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                Album album = mAlbumsList.get(position);
                mAlbumPlayer.playAlbum(album.getId(), 104);
                setPlayerVisible(true);
            }
        });
    }
    
    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mAlbumPlayer = new AlbumPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            mAlbumPlayer.addPlayerListener(this);
            setAttachedPlayer(mAlbumPlayer);
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
     * Search for all albums splitted by genre
     */
    private void getUserAlbums() {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserAlbums();
        mDeezerConnect.requestAsync(request, new JsonRequestListener() {
            
            @SuppressWarnings("unchecked")
            @Override
            public void onResult(final Object result, final Object requestId) {
                
                mAlbumsList.clear();
                
                try { 
                    mAlbumsList.addAll((List<Album>) result);
                }
                catch (ClassCastException e) {
                    handleError(e);
                }
                
                if (mAlbumsList.isEmpty()) {
                    Toast.makeText(UserAlbumsActivity.this, getResources()
                            .getString(R.string.no_results), Toast.LENGTH_LONG).show();
                }
                
                mAlbumsAdapter.notifyDataSetChanged();
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
    }
    
    @Override
    protected void onSkipToNextTrack() {
        mAlbumPlayer.skipToNextTrack();
    }
    
    @Override
    protected void onSkipToPreviousTrack() {
        mAlbumPlayer.skipToPreviousTrack();
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
