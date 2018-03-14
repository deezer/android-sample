package com.deezer.sdk.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.RadioCategory;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.RadioCategoriesRequestListener;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.RadioPlayer.RadioType;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.deezer.sdk.sample.ui.RadioCategoryAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays a list of deezer Radios and lets the user plays them
 *
 * @author Deezer
 */
public class RadiosActivity extends PlayerActivity implements RadioPlayerListener {

    /**
     * The list of radio categories displayed by this activity.
     */
    private List<RadioCategory> mRadioCategoryList = new ArrayList<RadioCategory>();

    /**
     * The adapter of the main list view.
     */
    private RadioCategoryAdapter mRadioCategoryAdapter = new RadioCategoryAdapter(this,
            mRadioCategoryList);

    /**
     * the Radio Player
     */
    private RadioPlayer mRadioPlayer;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore existing deezer Connection
        new SessionStore().restore(mDeezerConnect, this);


        // setup UI
        setContentView(R.layout.activity_radios);
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
        } catch (DeezerError e) {
            handleError(e);
        } catch (TooManyPlayersExceptions e) {
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
        // setup the 
        final ExpandableListView listViewRadioList = (ExpandableListView) findViewById(android.R.id.list);
        listViewRadioList.setAdapter(mRadioCategoryAdapter);
        listViewRadioList.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(final ExpandableListView parent, final View v,
                                        final int groupPosition,
                                        final int childPosition, final long id) {
                Radio radio = mRadioCategoryAdapter.getChild(groupPosition, childPosition);

                mRadioPlayer.playRadio(RadioType.RADIO, radio.getId());
                setPlayerVisible(true);
                return true;
            }
        });
    }

    /**
     * Search for all radios splitted by genre
     */
    private void searchAllRadioCategory() {
        DeezerRequest request = DeezerRequestFactory.requestRadiosCategories();
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect,
                new RadioCategoriesRequestListener() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(final Object result, final Object requestId) {

                        mRadioCategoryList.clear();

                        try {
                            mRadioCategoryList.addAll((List<RadioCategory>) result);
                        } catch (ClassCastException e) {
                            handleError(e);
                        }
                        if (mRadioCategoryList.isEmpty()) {
                            Toast.makeText(RadiosActivity.this, getResources()
                                    .getString(R.string.no_results), Toast.LENGTH_LONG).show();
                        }

                        mRadioCategoryAdapter.notifyDataSetChanged();
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
    public void onAllTracksEnded() {
    }

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {
        if(playableEntity.getType()=="track")
            displayTrack((Track)playableEntity);
    }

    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {

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
