package com.stream.jerye.queue.room;

import com.stream.jerye.queue.room.musicPage.SimpleTrack;

public interface EchoPlayer {

    void play();

    void pause();

    void resume();

    void seekTo(int newPosition);

    void previous();

    void next();

    boolean isPlaying();

    boolean isPaused();

    void release();

    void addTrack(SimpleTrack newTrack);


}
