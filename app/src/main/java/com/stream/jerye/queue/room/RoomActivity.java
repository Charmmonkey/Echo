package com.stream.jerye.queue.room;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.squareup.picasso.Picasso;
import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;
import com.stream.jerye.queue.lobby.LobbyActivity;
import com.stream.jerye.queue.lobby.User;
import com.stream.jerye.queue.profile.SpotifyProfileAsyncTask;
import com.stream.jerye.queue.room.messagePage.MessageFragment;
import com.stream.jerye.queue.room.musicPage.MusicFragment;
import com.stream.jerye.queue.room.musicPage.SimpleTrack;

import butterknife.BindView;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class RoomActivity extends AppCompatActivity implements
        MusicPlayerListener,
        FirebaseEventBus.FirebaseQueueAdapterHandler,
        FirebaseEventBus.FirebaseUserAdapterHandler,
        SpotifyProfileAsyncTask.SpotifyProfileCallback {
    private EchoPlayer mPlayer;
    private String TAG = "MainActivity.java", TRACK_KEY = "track instance";
    private String mToken, mRoomTitle, intentAction, mTrackTitle, mTrackArtist, mTrackAlbumImage;
    private AnimatedVectorDrawable playToPause, pauseToPlay;
    private FirebaseEventBus.MusicDatabaseAccess mMusicDatabaseAccess;
    private FirebaseEventBus.UserDatabaseAccess mUserDatabaseAccess;
    private UserAdapter mUserAdapter;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            mPlayer = ((PlayerService.PlayerBinder) service).getService(RoomActivity.this, RoomActivity.this, mToken);
            Log.d(TAG, "peeking from activity result");
//            mMusicDatabaseAccess.peek();
            mMusicDatabaseAccess.addChildListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service DisConnected");

            mPlayer = null;
        }
    };

    @BindView(R.id.room_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.room_toolbar_profile)
    ImageView mToolbarProfile;
    @BindView(R.id.room_toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.view_pager)
    ViewPager mPager;
    @BindView(R.id.previous_button)
    ImageView mPreviousButton;
    @BindView(R.id.play_button)
    ImageView mPlayButton;
    @BindView(R.id.next_button)
    ImageView mNextButton;
    @BindView(R.id.music_seekbar)
    SeekBar mSeekBar;
    @BindView(R.id.music_current_title)
    TextView mCurrentMusicTitle;
    @BindView(R.id.music_current_artist)
    TextView mCurrentMusicArtist;
    @BindView(R.id.current_album_image)
    ImageView mCurrentAlbumImage;
    @BindView(R.id.music_duration)
    TextView mMusicDuration;
    @BindView(R.id.music_progress)
    TextView mMusicProgress;
    @BindView(R.id.profile_drawer)
    DrawerLayout mDrawer;
    @BindView(R.id.profile_name)
    TextView mProfileName;
    @BindView(R.id.profile_picture)
    ImageView mProfilePicture;
    @BindView(R.id.profile_logout)
    TextView mProfileLogoutButton;
    @BindView(R.id.users_list)
    RecyclerView mUsersList;
    @BindView(R.id.room_toolbar_page_icon)
    ImageView mToolbarIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_activity);
        ButterKnife.bind(this);

        PreferenceUtility.initialize(this);
        mToken = PreferenceUtility.getPreference(PreferenceUtility.SPOTIFY_TOKEN);

        Intent intent = getIntent();
        intentAction = intent.getAction();


        mMusicDatabaseAccess = new FirebaseEventBus.MusicDatabaseAccess(this, this);
        mUserDatabaseAccess = new FirebaseEventBus.UserDatabaseAccess(this, this);

        playToPause = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_play_to_pause);
        pauseToPlay = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_pause_to_play);

        mPager.setAdapter(new SimpleFragmentPageAdapter(getSupportFragmentManager()));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0 ){
                    mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(RoomActivity.this,R.drawable.chat_icon));
                }else{
                    mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(RoomActivity.this,R.drawable.music_icon));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        SpotifyProfileAsyncTask asyncTask = new SpotifyProfileAsyncTask(this, this, mToken);
        asyncTask.execute();
        mUserDatabaseAccess.getUsers();

        if (LobbyActivity.ACTION_NEW_USER.equals(intentAction)) {
            Bundle bundle = intent.getExtras();
            mRoomTitle = bundle.getString("room title");
        } else if (LobbyActivity.ACTION_EXISTING_USER.equals(intentAction)) {
            mRoomTitle = PreferenceUtility.getPreference(PreferenceUtility.ROOM_TITLE);
        }

        setActionBar(mToolbar);
        mToolbarTitle.setText(mRoomTitle);

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(PlayerService.getIntent(this), mServiceConnection, Activity.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "FirebaseInstanceId Token: " + FirebaseInstanceId.getInstance().getToken());
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "play button clicked");

                if (mPlayer == null) {
                    Log.d("MainActivity.java", "mPlayer is null");
                    return;
                }

                if (mPlayer.isPaused()) {
                    Log.d(TAG, "isPaused");
                    mPlayer.resume();
                    mPlayButton.setImageDrawable(playToPause);
                    playToPause.start();
                } else if (mPlayer.isPlaying()) {
                    Log.d(TAG, "isPlaying");
                    mPlayer.pause();
                    mPlayButton.setImageDrawable(pauseToPlay);
                    pauseToPlay.start();
                } else {
                    Log.d(TAG, "else");
                    mPlayer.play();
                    mPlayButton.setImageDrawable(playToPause);
                    playToPause.start();
                }
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.resume();
            }
        });

        mUserAdapter = new UserAdapter(this);
        mUsersList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mUsersList.setAdapter(mUserAdapter);

    }


    @Override
    public void createProfile(UserPrivate userPrivate) {
        String profileName = userPrivate.display_name==null ? "Anonymous" : userPrivate.display_name;
        String profileId = userPrivate.id;
        String profilePicture;
        try {
            profilePicture = userPrivate.images.get(0).url;
        } catch (IndexOutOfBoundsException e) {
            profilePicture = "";
        }


        String[] profile = {profileName, profilePicture, profileId};

        PreferenceUtility.setPreference(PreferenceUtility.PROFILE_GENERIC, profile);

        mProfileName.setText(profileName);

        if(!profilePicture.equals("")){
            Picasso.with(this).load(profilePicture).into(mProfilePicture);
        }else{
            mProfilePicture.setImageDrawable(getDrawable(R.drawable.default_profile_icon));

        }

        // Check if user is unique first
        if (LobbyActivity.ACTION_NEW_USER.equals(intentAction)) {
            User newUser = new User(profileName, profileId, FirebaseInstanceId.getInstance().getToken(), profilePicture);
            mUserDatabaseAccess.push(newUser);
        }
    }

    @Override
    public void getUser(User user) {
        mUserAdapter.addUser(user);
    }

    private class SimpleFragmentPageAdapter extends FragmentStatePagerAdapter {

        private SimpleFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return MusicFragment.newInstance();
            } else {
                return MessageFragment.newInstance();
            }

        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }


    }


//    @Override
//    public void peekedResult(List<SimpleTrack> list) {
//        mPlayer.setNextTrack(list);
//    }

    @Override
    public void getSongProgress(int positionInMs) {
        mSeekBar.setProgress(positionInMs);
        int totalInS = positionInMs / 1000;
        int minutes = totalInS / 60;
        int seconds = totalInS % 60;
        mMusicProgress.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    }

    @Override
    public void getSongDuration(int durationInMs) {
        Log.d(TAG, "setting max: " + durationInMs);
        mSeekBar.setMax(durationInMs);

        int totalInS = durationInMs / 1000;
        int minutes = totalInS / 60;
        int seconds = totalInS % 60;
        mMusicDuration.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    }

    @Override
    public void displayCurrentTrack(SimpleTrack simpleTrack) {
        mTrackTitle = simpleTrack.getName();
        mTrackArtist = simpleTrack.getArtistName();
        mTrackAlbumImage = simpleTrack.getAlbumImage();

        mCurrentMusicTitle.setText(mTrackTitle);
        mCurrentMusicArtist.setText(mTrackArtist);
        Picasso.with(this).load(mTrackAlbumImage).into(mCurrentAlbumImage);
    }


    @Override
    public void enqueue(SimpleTrack simpleTrack) {
        mPlayer.addTrack(simpleTrack);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(mServiceConnection);

    }

    public void profileLogout(View v) {
        AuthenticationClient.clearCookies(this);

        mUserDatabaseAccess.removeUser();

        PreferenceUtility.deleteSpotifyPreferences();
        PreferenceUtility.deleteRoomPreference();
        PreferenceUtility.deleteUserPreference();

        Intent exit = new Intent(this, LobbyActivity.class);
        startActivity(exit);
    }


    public void openProfileDrawer(View v) {
        mDrawer.openDrawer(Gravity.START);
    }

    public void playNext(View v) {
        if (mPlayer == null) {
            Log.d("MainActivity.java", "mPlayer is null");
            return;
        }
        mPlayer.next();
    }

    public void playPrevious(View v) {
        if (mPlayer == null) {
            Log.d("MainActivity.java", "mPlayer is null");
            return;
        }
        mPlayer.previous();
    }

    public void viewPagerSwipe(View v){
        if(mPager.getCurrentItem() == 0 ){
            mPager.setCurrentItem(1);
            mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.music_icon));

        }else{
            mPager.setCurrentItem(0);
            mToolbarIcon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.chat_icon));

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(TRACK_KEY, new String[] {mTrackTitle, mTrackArtist, mTrackAlbumImage});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getStringArray(TRACK_KEY) != null){
            String[] trackInfo = savedInstanceState.getStringArray(TRACK_KEY);
            mCurrentMusicTitle.setText(trackInfo[0]);
            mCurrentMusicArtist.setText(trackInfo[1]);
            Picasso.with(this).load(trackInfo[2]).into(mCurrentAlbumImage);
        }


    }
}
