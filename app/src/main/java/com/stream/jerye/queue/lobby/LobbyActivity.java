package com.stream.jerye.queue.lobby;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.stetho.Stetho;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.stream.jerye.queue.BuildConfig;
import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.room.RoomActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LobbyActivity extends AppCompatActivity {
    private String mToken;
    public static final String CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID;
    private static final String REDIRECT_URI = "https://en.wikipedia.org/wiki/Whitelist";
    public static final String ACTION_EXISTING_USER = "com.stream.jerye.queue.ACTION_EXISTING_USER";
    public static final String ACTION_NEW_USER = "com.stream.jerye.queue.ACTION_NEW_USER";

    private static final int REQUEST_CODE = 42;
    private PreferenceUtility prefs;
    private String roomKey;


    @BindView(R.id.lobby_create_button)
    ImageView mCreateRoomButton;
    @BindView(R.id.lobby_join_button)
    ImageView mJoinRoomButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_activity);

        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);
        PreferenceUtility.initialize(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        roomKey = PreferenceUtility.getPreference(PreferenceUtility.ROOM_KEY);
        if (!roomKey.equals("")) {
            mJoinRoomButton.setImageDrawable(getDrawable(R.drawable.ic_refresh_black_24dp));
        }
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

                mCreateRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                mCreateRoomButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new CreateRoomDiaglog().show(getFragmentManager(), "CreateRoomDialog");
                    }
                });

                mJoinRoomButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                mJoinRoomButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!roomKey.equals("")) {
                            Intent intent = new Intent(LobbyActivity.this, RoomActivity.class)
                                    .setAction(ACTION_EXISTING_USER);
                            startActivity(intent);
                        } else {
                            new JoinRoomDialog().show(getFragmentManager(), "JoinRoomDialog");

                        }
                    }
                });

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

    public void clearRoomPreference(View v) {
//        prefs.edit().putString("room key", "").apply();
//        roomKey = "";
        mJoinRoomButton.setImageDrawable(getDrawable(R.drawable.ic_play));
    }

}
