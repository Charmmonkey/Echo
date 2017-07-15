package com.stream.jerye.queue.room;

import com.stream.jerye.queue.room.musicPage.SimpleTrack;

/**
 * Created by jerye on 6/16/2017.
 */

public interface MusicPlayerListener {

//    void queueNextSong(SimpleTrack oldTrackToRemove);
    void getSongProgress(int positionInMs);
    void getSongDuration(int durationInMs);
    void displayCurrentTrack(SimpleTrack simpleTrack);
}
