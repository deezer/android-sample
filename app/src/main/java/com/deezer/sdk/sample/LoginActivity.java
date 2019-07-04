package com.deezer.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;


/**
 * Presents two buttons, one to log in with a deezer account and access a user's playlists, the
 * other to play deezer radios
 *
 * @author Deezer
 */
public class LoginActivity extends BaseActivity {

    /**
     * Permissions requested on Deezer accounts.
     * <p/>
     * cf : http://developers.deezer.com/api/permissions
     */
    protected static final String[] PERMISSIONS = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.OFFLINE_ACCESS
    };


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        findViewById(R.id.buttonVisualizerFFT).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, VisualizerActivity.class);
                intent.putExtra(VisualizerActivity.EXTRA_DISPLAY, VisualizerActivity.DISPLAY_FFT);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonVisualizerWaveform).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, VisualizerActivity.class);
                intent.putExtra(VisualizerActivity.EXTRA_DISPLAY,
                        VisualizerActivity.DISPLAY_WAVEFORM);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonEqualizer).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, EqualizerActivity.class);

                startActivity(intent);
            }
        });

        findViewById(R.id.buttonRadio).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this, RadiosActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonLogin).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                connectToDeezer();
            }
        });

        SessionStore sessionStore = new SessionStore();

        if (sessionStore.restore(mDeezerConnect, this)) {
            Toast.makeText(this, "Already logged in !", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        ((TextView) findViewById(R.id.textVersion)).setText(DeezerConnect.SDK_VERSION);
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
        public void onComplete(final Bundle values) {
            // store the current authentication info 
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mDeezerConnect, LoginActivity.this);

            // Launch the Home activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        @Override
        public void onException(final Exception exception) {
            Toast.makeText(LoginActivity.this, R.string.deezer_error_during_login,
                    Toast.LENGTH_LONG).show();
        }


        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, R.string.login_cancelled, Toast.LENGTH_LONG).show();
        }


    };
}
