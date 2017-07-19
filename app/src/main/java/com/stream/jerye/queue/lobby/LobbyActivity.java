package com.stream.jerye.queue.lobby;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.stream.jerye.queue.BuildConfig;
import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;
import com.stream.jerye.queue.room.RoomActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LobbyActivity extends AppCompatActivity {
    private String mToken = "";
    public static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "https://en.wikipedia.org/wiki/Whitelist";
    public static final String ACTION_EXISTING_USER = "com.stream.jerye.queue.ACTION_EXISTING_USER";
    public static final String ACTION_NEW_USER = "com.stream.jerye.queue.ACTION_NEW_USER";
    private AnimatedVectorDrawable returnToJoin;
    private static boolean loggedInFlag = false;


    private static final int REQUEST_CODE = 42;
    private String roomKey;
    private FirebaseEventBus.UserDatabaseAccess mUserDatabaseAccess;

    @BindView(R.id.lobby_create_button)
    ImageView mCreateRoomButton;
    @BindView(R.id.lobby_join_button)
    ImageView mJoinRoomButton;
    @BindView(R.id.lobby_clear_button)
    ImageView mClearRoomButton;
    @BindView(R.id.create_word)
    TextView mCreateWord;
    @BindView(R.id.return_word)
    TextView mReturnWord;
    @BindView(R.id.exit_word)
    TextView mExitWord;
    @BindView(R.id.join_word)
    TextView mJoinWord;
    @BindView(R.id.lobby_login_button)
    TextView mLoginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_activity);

        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);
        PreferenceUtility.initialize(this);

        returnToJoin = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_return_to_join);
        mUserDatabaseAccess = new FirebaseEventBus.UserDatabaseAccess(this);

        layoutLoggedOut();

        mCreateRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mToken.equals("")) {
                    new CreateRoomDiaglog().show(getFragmentManager(), "CreateRoomDialog");
                } else {
                    Toast.makeText(LobbyActivity.this, getString(R.string.lobby_login_first_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mJoinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mToken.equals("")) {
                    roomKey = PreferenceUtility.getPreference(PreferenceUtility.ROOM_KEY);
                    if (!roomKey.equals("")) {
                        Intent intent = new Intent(LobbyActivity.this, RoomActivity.class)
                                .setAction(ACTION_EXISTING_USER);
                        startActivity(intent);
                    } else {
                        new JoinRoomDialog().show(getFragmentManager(), "JoinRoomDialog");
                    }
                } else {
                    Toast.makeText(LobbyActivity.this, getString(R.string.lobby_login_first_message), Toast.LENGTH_SHORT).show();
                }

            }
        });

        mClearRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mToken.equals("")) {
                    mUserDatabaseAccess.removeUser();
                    PreferenceUtility.deleteRoomPreference();
                    PreferenceUtility.deleteUserPreference(); //Since room preference is deleted, next user key will be different
                    mJoinRoomButton.setImageDrawable(returnToJoin);
                    returnToJoin.start();
                    mJoinWord.setVisibility(View.VISIBLE);
                    mReturnWord.setVisibility(View.INVISIBLE);
                    mExitWord.setVisibility(View.GONE);
                    mClearRoomButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(LobbyActivity.this, getString(R.string.lobby_login_first_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRoomPreference();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("MainActivity.java", "onActivityResult: " + requestCode);

        if (requestCode == REQUEST_CODE) {
            Log.d("MainActivity.java", "requestcode");

            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.d("MainActivity.java", "type: " + response.getType());
                mToken = response.getAccessToken();

                PreferenceUtility.setPreference(PreferenceUtility.SPOTIFY_TOKEN, mToken);

                layoutLoggedIn();

            }
        }
    }

    public void loginButton(View v) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


    }

    public void layoutLoggedOut() {
        loggedInFlag = false;
        checkRoomPreference();

        mCreateRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mJoinRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        mClearRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        mCreateWord.setVisibility(View.INVISIBLE);
        mExitWord.setVisibility(View.INVISIBLE);
        mReturnWord.setVisibility(View.INVISIBLE);
        mJoinWord.setVisibility(View.INVISIBLE);

    }

    public void layoutLoggedIn() {
        loggedInFlag = true;
        checkRoomPreference();

        mCreateWord.setVisibility(View.VISIBLE);
        mExitWord.setVisibility(View.VISIBLE);
        mReturnWord.setVisibility(View.VISIBLE);
        mJoinWord.setVisibility(View.VISIBLE);


        mCreateRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        mJoinRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        mClearRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.white));


        mLoginButton.setText(getResources().getString(R.string.lobby_loggedin));
        mLoginButton.setAllCaps(true);


    }

    public void checkRoomPreference() {
        roomKey = PreferenceUtility.getPreference(PreferenceUtility.ROOM_KEY);
        if (!roomKey.equals("")) {
            mJoinRoomButton.setImageDrawable(getDrawable(R.drawable.rejoin_room_icon));
            mClearRoomButton.setVisibility(View.VISIBLE);
            mJoinWord.setVisibility(View.INVISIBLE);

            if(loggedInFlag){
                mReturnWord.setVisibility(View.VISIBLE);
                mExitWord.setVisibility(View.VISIBLE);
            }


        } else {
            mJoinRoomButton.setImageDrawable(getDrawable(R.drawable.join_room_icon));
            mReturnWord.setVisibility(View.INVISIBLE);
            mExitWord.setVisibility(View.INVISIBLE);
            mClearRoomButton.setVisibility(View.GONE);
        }
    }


}
