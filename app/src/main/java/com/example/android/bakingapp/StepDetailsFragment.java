package com.example.android.bakingapp;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.bakingapp.RecipieObjects.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivars on 3/17/18.
 */

public class StepDetailsFragment extends Fragment implements ExoPlayer.EventListener{

    private static final String TAG = StepDetailsFragment.class.getSimpleName();
    private static final String INTENT_EXTRA_STEPS_KEY = "steps";
    private static final String INTENT_EXTRA_STEP_SELECTED_KEY = "step_clicked";
    private static final String MEDIA_SESSION_TAG = "media_session_tag";
    //save instance keys
    private static final String SAVE_INSTANCE_STEP_NUMBER = "step_number";
    private static final String SAVE_INSTANCE_PLAYER_POS = "player_position";
    private static final long NO_POSITION_SAVED = -1;
    //a media session is needed so the exoPlayer can be controlled from external devices
    private static MediaSessionCompat mMediaSession;
    @BindView(R.id.exo_player_view)
    SimpleExoPlayerView exoPlayerView;
    @BindView(R.id.no_step_video_iv)
    ImageView noVideoIv;
    @BindView(R.id.step_descrption_tv)
    TextView stepDescriptionTv;
    @BindView(R.id.previous_step_button)
    ImageButton previousStepIb;
    @BindView(R.id.next_step_button)
    ImageButton nextStepIb;
    @Nullable
    @BindView(R.id.landsscape_layout_sv)
    ScrollView landscapeSv;
    private ArrayList<Step> mSteps;
    //used for showing the correct step
    private int mCurrentStepNumber;
    private SimpleExoPlayer mExoPlayer;
    private PlaybackStateCompat.Builder mStateBuilder;
    //Variable to save exoPlayer position
    private long exoplayerPosition = NO_POSITION_SAVED;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_step_detail_view, null, false);
        ButterKnife.bind(this, rootView);

        //the UI change from orientation only on a phone
        if (!getResources().getBoolean(R.bool.open_in_tablet)) {
            setupUiFromOrientation();
        }

        mSteps = (ArrayList)getArguments().getSerializable(INTENT_EXTRA_STEPS_KEY);
        if (savedInstanceState == null){
            mCurrentStepNumber = getArguments().getInt(INTENT_EXTRA_STEP_SELECTED_KEY);
        } else {
            mCurrentStepNumber = savedInstanceState.getInt(SAVE_INSTANCE_STEP_NUMBER);
            if (savedInstanceState.containsKey(SAVE_INSTANCE_PLAYER_POS)) {
                exoplayerPosition = savedInstanceState.getLong(SAVE_INSTANCE_PLAYER_POS);
            }
        }

        stepDescriptionTv.setText(mSteps.get(mCurrentStepNumber).getDescription());
        setUpUiWithStepContent();

        previousStepIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentStepNumber--;
                if(mCurrentStepNumber == mSteps.size() - 2){
                    //The next button vas Invisible in the last step, turn it on now
                    nextStepIb.setVisibility(View.VISIBLE);
                }
                setUpUiWithStepContent();
                scrollToTop();
            }
        });

        nextStepIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentStepNumber++;
                if(mCurrentStepNumber == 1){
                    //The previous button vas Invisible in step 0, turn it on now
                    previousStepIb.setVisibility(View.VISIBLE);
                }
                setUpUiWithStepContent();
                scrollToTop();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_INSTANCE_STEP_NUMBER, mCurrentStepNumber);
        if (exoplayerPosition != NO_POSITION_SAVED) {
            outState.putLong(SAVE_INSTANCE_PLAYER_POS, exoplayerPosition);
        }
    }



    //Scroll to top if in landscape mode on step change
    private void scrollToTop(){
        if (landscapeSv != null){
            //add a small delay to this operation so the view is scrolled when the UI is populated
            //this is a bad way of doing it since an arbitrary delay must be chosen
            //trying to use getViewTreeObserver().addOnGlobalLayoutListener did not work always
            landscapeSv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    landscapeSv.fullScroll(ScrollView.FOCUS_UP);
                }
            }, 25);
        }
    }

    //Sets up the UI dependant from phone orientation
    private void setupUiFromOrientation(){
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //If the orientation is landscape, make the video take the full height of the screen
            //and hide the actionBar
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            //usable display height is the display height minus the statusbar height
            int displayHeight = displayMetrics.heightPixels - getStatusBarHeight();

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)exoPlayerView.getLayoutParams();
            params.height = displayHeight;
            noVideoIv.setLayoutParams(params);

        } else {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setUpUiWithStepContent(){
        Step currentStep = mSteps.get(mCurrentStepNumber);
        String stepVideoUrl = currentStep.getVideoUrl();
        //If the current step has a video then display it with exo player, else show default icon
        if (stepVideoUrl != null && !stepVideoUrl.isEmpty()){
            exoPlayerView.setVisibility(View.VISIBLE);
            noVideoIv.setVisibility(View.GONE);
            setUpMediaSession();
            initializePlayer(stepVideoUrl);
        } else {
            releasePlayer();
            exoPlayerView.setVisibility(View.GONE);
            noVideoIv.setVisibility(View.VISIBLE);
        }

        stepDescriptionTv.setText(currentStep.getDescription());

        if(mCurrentStepNumber == 0){
            previousStepIb.setVisibility(View.INVISIBLE);
        } else if(mCurrentStepNumber == mSteps.size() - 1){
            nextStepIb.setVisibility(View.INVISIBLE);
        }
    }

    private void initializePlayer (String stepVideoUrl){
        if (mExoPlayer == null){
            //Create an exoplayer
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            exoPlayerView.setPlayer(mExoPlayer);
            //Add the state change listener, so if the state change in the exoplayer it is synced with media session
            mExoPlayer.addListener(this);
        }
        Uri videoUri = Uri.parse(stepVideoUrl);
        MediaSource mediaSource = buildMediaSource(videoUri);
        mExoPlayer.prepare(mediaSource,true,false);
        mExoPlayer.setPlayWhenReady(true);
        if (exoplayerPosition != NO_POSITION_SAVED) {
            mExoPlayer.seekTo(exoplayerPosition);
        }
    }

    private MediaSource buildMediaSource(Uri videoUri) {
        return new ExtractorMediaSource(
                videoUri,
                new DefaultHttpDataSourceFactory("baking_app"),
                new DefaultExtractorsFactory(),
                null,
                null
        );
    }

    private void setUpMediaSession(){
        if (mMediaSession == null){
            //create a media session
            mMediaSession = new MediaSessionCompat(getContext(), MEDIA_SESSION_TAG);
            //set flags on the sesion
            // Enable callbacks from MediaButtons and TransportControls.
            mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            // Do not let MediaButtons restart the player when the app is not visible.
            mMediaSession.setMediaButtonReceiver(null);
            //Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
            mStateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(
                            PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                    PlaybackStateCompat.ACTION_PAUSE |
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    );
            mMediaSession.setPlaybackState(mStateBuilder.build());
            mMediaSession.setCallback(new MySessionCallback());
            mMediaSession.setActive(true);
        }
    }

    private void releasePlayer(){
        if (mExoPlayer != null){
            //save position in variable so it can be saved in onSaveInstanceState
            exoplayerPosition = mExoPlayer.getCurrentPosition();
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //release player so playback does not continue after app is no longer visible
        releasePlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //deactivate the media session when we exit the fragment
        if (mMediaSession != null){
            mMediaSession.setActive(false);
            //set media session to null so it can be initialized when another step is opened
            mMediaSession = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpUiWithStepContent();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    //called when exoplayer state changes(pause, play etc)
    //playWhenReady true if is playing false if not
    //can be ready,idle,buffering
    //we use this to update the mediaSession state
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == ExoPlayer.STATE_READY && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if (playbackState == ExoPlayer.STATE_READY){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    //these methods will be called by external clients on the media session
    //these methods are used to interface with the exoplayer
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }
}
