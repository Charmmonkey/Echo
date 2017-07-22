package com.stream.jerye.queue.widget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;
import com.stream.jerye.queue.room.musicPage.SimpleTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerye on 7/8/2017.
 */

public class QueueWidgetRemoteService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("Widget", "RemoteViewsService instance present");
        Bundle bundle = intent.getExtras();
        int[] appWidgetIds = bundle.getIntArray("appWidgetIds");
        return new QueueRemoteViewsFactory(appWidgetIds);
    }



    public class QueueRemoteViewsFactory implements RemoteViewsFactory, FirebaseEventBus.FirebaseQueueAdapterHandler{
        private FirebaseEventBus.MusicDatabaseAccess mMusicDatabaseAccess;
        private List<SimpleTrack> mList = new ArrayList<>();
        private int[] appWidgetIdsArray;
        private String TAG = "Widget";
        private boolean widgetConnectedFlag = false;



        @Override
        public void enqueue(SimpleTrack simpleTrack) {
            mList.add(simpleTrack);
        }


        public QueueRemoteViewsFactory(int[] appWidgetIds){
            appWidgetIdsArray = appWidgetIds;

            PreferenceUtility.initialize(getApplicationContext());
            mMusicDatabaseAccess = new FirebaseEventBus.MusicDatabaseAccess(getApplicationContext(),this);
            mMusicDatabaseAccess.addWidgetUpdater();

        }

        @Override
        public void onCreate() {
            Log.d(TAG, "onCreate");

        }

        @Override
        public int getCount() {

            return mList.size();
        }

        @Override
        public void onDataSetChanged() {
            Log.d(TAG, "DataSetChanged");
        }

        @Override
        public void onDestroy() {
            mMusicDatabaseAccess.removeWidgetUpdater();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            SimpleTrack track = mList.get(position);
            String artistName = track.getArtistName();
            String trackName = track.getName();
            RemoteViews widgetItemView;

            widgetItemView = new RemoteViews(getPackageName(), R.layout.widget_list_item);

            widgetItemView.setTextViewText(R.id.widget_queued_music_name,trackName);
            widgetItemView.setTextViewText(R.id.widget_queued_music_artists,artistName);

            return widgetItemView;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public RemoteViews getLoadingView() {

            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}


