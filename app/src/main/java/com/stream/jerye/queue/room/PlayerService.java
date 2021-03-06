package com.stream.jerye.queue.room;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PlayerService extends Service {

    private final IBinder mBinder = new PlayerBinder();
    private EchoPlayer mEchoPlayer;

    public static Intent getIntent(Context context) {
        return new Intent(context, PlayerService.class);
    }

    public class PlayerBinder extends Binder {
        public EchoPlayer getService(Context context, MusicPlayerListener musicPlayerListener, String spotifyAccessToken) {
            mEchoPlayer = new MultiMediaPlayer(context, musicPlayerListener, spotifyAccessToken);
            return mEchoPlayer;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mEchoPlayer.release();
        super.onDestroy();
    }
}
