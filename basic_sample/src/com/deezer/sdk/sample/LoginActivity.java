package com.deezer.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogError;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.OAuthException;


/**
 * Presents two buttons, one to log in with a deezer account and access a user's playlists, the
 * other to play deezer radios
 * 
 * @author Deezer
 * 
 */
public class LoginActivity extends BaseActivity {
    
    /**
     * Permissions requested on Deezer accounts.
     * 
     * cf : http://developers.deezer.com/api/permissions
     */
    protected static final String[] PERMISSIONS = new String[] {
            "basic_access", "offline_access"
    };
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_login);
        
        findViewById(R.id.buttonRadio).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, RadiosActivity.class);
                startActivity(intent);
            }
        });
        
        findViewById(R.id.buttonLogin).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                connectToDeezer();
            }
        });
        
        SessionStore sessionStore = new SessionStore();
        
        if (sessionStore.restore(mDeezerConnect, this)) {
            Toast.makeText(this, "Already logged in !", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }
    
    /**
     * Asks the SDK to display a log in dialog for the user
     */
    private void connectToDeezer() {
        mDeezerConnect.authorize(this, PERMISSIONS, mDeezerDialogListener);
    }
    
    /**
     * A listener for the Deezer Login Dialog
     */
    private DialogListener mDeezerDialogListener = new DialogListener() {
        
        @Override
        public void onComplete(Bundle values) {
            // store the current authentication info 
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mDeezerConnect, LoginActivity.this);
            
            // Launch the Home activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        
        @Override
        public void onDeezerError(final DeezerError deezerError) {
            Toast.makeText(LoginActivity.this, R.string.deezer_error_during_login,
                    Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onError(final DialogError dialogError) {
            Toast.makeText(LoginActivity.this, R.string.deezer_error_during_login,
                    Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, R.string.login_cancelled, Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onOAuthException(OAuthException oAuthException) {
            Toast.makeText(LoginActivity.this, R.string.invalid_credentials, Toast.LENGTH_LONG)
                    .show();
        }
    };
}
