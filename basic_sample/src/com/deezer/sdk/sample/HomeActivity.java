package com.deezer.sdk.sample;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.deezer.sdk.model.User;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.OAuthException;
import com.deezer.sdk.sample.task.FetchUserThumbnailTask;
import com.deezer.sdk.sample.task.FetchUserThumbnailTask.UserThumbnailTaskListener;


/**
 * Displays a user home (playlists, albums, artists, tracks, ...)
 * 
 * @author Deezer
 * 
 */
public class HomeActivity extends BaseActivity {
    
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_home);
        
        // Restore authentication   
        new SessionStore().restore(mDeezerConnect, this);
        
        // setup the user home
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(
                        R.array.user_data));
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                userNavigate(position);
            }
        });
        
        
        // Get user info from Deezer
        fetchUserInfo();
    }
    
    private void displayUserInfo(final User user) {
        ((TextView) findViewById(R.id.name)).setText(user.getLastName());
        ((TextView) findViewById(R.id.first_name)).setText(user.getFirstName());
        
        Date birthday = user.getBirthday();
        SimpleDateFormat format = new SimpleDateFormat("yyyy, MMMM dd");
        ((TextView) findViewById(R.id.bithday)).setText(format.format(birthday));
        
        
        setTitle(getString(R.string.activity_home, user.getName()));
        
        
        FetchUserThumbnailTask task = new FetchUserThumbnailTask(this,
                new UserThumbnailTaskListener() {
                    
                    @Override
                    public void thumbnailLoaded(final User user, final Drawable drawable) {
                        ((ImageView) findViewById(R.id.user_picture)).setImageDrawable(drawable);
                    }
                });
        task.execute(user);
    }
    /**
     * Fetches the user info from Deezer API
     */
    private void fetchUserInfo() {
        DeezerRequest request = DeezerRequestFactory.requestCurrentUser();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect, new JsonRequestListener() {
            
            @Override
            public void onResult(final Object result, final Object requestId) {
                if (result instanceof User) {
                    displayUserInfo((User) result);
                } else {
                    handleError(new IllegalArgumentException());
                }
            }
            
            @Override
            public void onOAuthException(final OAuthException exception, final Object requestId) {
                handleError(exception);
            }
            
            @Override
            public void onMalformedURLException(final MalformedURLException exception,
                    final Object requestId) {
                handleError(exception);
            }
            
            @Override
            public void onIOException(final IOException exception, final Object requestId) {
                handleError(exception);
            }
            
            @Override
            public void onDeezerError(final DeezerError error, final Object requestId) {
                handleError(error);
            }
            
            @Override
            public void onJSONParseException(final JSONException exception, final Object requestId) {
                handleError(exception);
            }
        });
        
        task.execute(request);
    }
    
    private static final int PLAYLISTS = 0;
    private static final int ALBUMS = 1;
    private static final int ARTISTS = 2;
    private static final int RADIOS = 3;
    private static final int TRACKS = 4;
    private static final int CUSTOM = 5;
    
    /**
     * Navigates to another activity depending on what the user clicked on
     * 
     * @param selection
     */
    private void userNavigate(final int selection) {
        Intent intent = null;
        
        switch (selection) {
            case PLAYLISTS:
                intent = new Intent(this, UserPlaylistsActivity.class);
                break;
            case ALBUMS:
                intent = new Intent(this, UserAlbumsActivity.class);
                break;
            case ARTISTS:
                intent = new Intent(this, UserArtistsActivity.class);
                break;
            case RADIOS:
                intent = new Intent(this, UserRadiosActivity.class);
                break;
            case TRACKS:
                intent = new Intent(this, UserTopTracksActivity.class);
                break;
            case CUSTOM:
                intent = new Intent(this, UserCustomTrackListActivity.class);
                break;
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
}

