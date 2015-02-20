package com.deezer.sdk.sample;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.AImageOwner.ImageSize;
import com.deezer.sdk.model.User;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.squareup.picasso.Picasso;


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
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        getMenuInflater().inflate(R.menu.notifications, menu);
        
        return true;
    }
    
    @Override
    @TargetApi(11)
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean res = true;
        
        switch (item.getItemId()) {
            case R.id.action_notification:
                promptUserNotification();
                break;
            default:
                res = super.onOptionsItemSelected(item);
                break;
        }
        
        return res;
    }
    
    private void displayUserInfo(final User user) {
        ((TextView) findViewById(R.id.name)).setText(user.getLastName());
        ((TextView) findViewById(R.id.first_name)).setText(user.getFirstName());
        
        Date birthday = user.getBirthday();
        SimpleDateFormat format = new SimpleDateFormat("yyyy, MMMM dd");
        ((TextView) findViewById(R.id.bithday)).setText(format.format(birthday));
        
        
        setTitle(getString(R.string.activity_home, user.getName()));
        
        Picasso.with(this).load(user.getImageUrl(ImageSize.medium))
                .into((ImageView) findViewById(R.id.user_picture));
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
            public void onUnparsedResult(final String response, final Object requestId) {
                handleError(new DeezerError("Unparsed reponse"));
            }
            
            
            @Override
            public void onException(final Exception exception, final Object requestId) {
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
    private static final int FLOW = 5;
    private static final int CUSTOM = 6;
    
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
            case FLOW:
                intent = new Intent(this, UserFlowActivity.class);
                break;
            case CUSTOM:
                intent = new Intent(this, UserCustomTrackListActivity.class);
                break;
        }
        
        if (intent != null) {
            startActivity(intent);
        }
    }
    
    private void promptUserNotification() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        
        alert.setTitle("Send notification");
        
        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);
        
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                String value = input.getText().toString();
                sendNotification(value);
            }
        });
        
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(final DialogInterface dialog, final int whichButton) {
                // Canceled.
            }
        });
        
        alert.show();
    }
    
    private void sendNotification(final String notification) {
        DeezerRequest request = DeezerRequestFactory
                .requestCurrentUserSendNotification(notification);
        mDeezerConnect.requestAsync(request, new RequestListener() {
            
            @Override
            public void onComplete(final String response, final Object requestId) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Notification sent", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                
            }
            
            @Override
            public void onException(final Exception exception, final Object requestId) {
                handleError(exception);
            }
            
            
        });
    }
}
