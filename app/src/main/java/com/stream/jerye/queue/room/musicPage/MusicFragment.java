package com.stream.jerye.queue.room.musicPage;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment implements Search.View,
        FirebaseEventBus.FirebaseQueueAdapterHandler,
        MusicQueueAdapter.MusicPlaylistClickHandler {
    private LinearLayoutManager mResultsLayoutManager = new LinearLayoutManager(getContext()), mQueueLayoutManager = new LinearLayoutManager(getContext());
    private ScrollListener mScrollListener = new ScrollListener(mResultsLayoutManager);
    private SearchResultsAdapter mSearchResultsAdapter;
    private View mRootView;
    private MusicQueueAdapter mQueueMusicAdapter;
    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";
    private Search.ActionListener mActionListener;
    private String TAG = "MainActivity.java";
    private String mSpotifyAccessToken;
    private FirebaseEventBus.MusicDatabaseAccess mMusicDatabaseAccess;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private boolean isTyping = false;

    @BindView(R.id.search_container)
    LinearLayout mSearchContainer;
    @BindView(R.id.search_view)
    SearchView mSearchView;
    @BindView(R.id.search_results)
    RecyclerView mMusicResultsList;
    @BindView(R.id.music_queue)
    RecyclerView mMusicQueueList;

    public MusicFragment() {
        // Required empty public constructor
    }

    public static MusicFragment newInstance() {
        Bundle args = new Bundle();
        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private class ScrollListener extends ResultListScrollListener {

        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void onLoadMore() {
            Log.d(TAG, "onscroll");
            mActionListener.loadMoreResults();
        }
    }

    @Override
    public void onClick() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "music frag create view");
        mRootView = inflater.inflate(R.layout.music_fragment, container, false);
        ButterKnife.bind(this, mRootView);
        // Setup search field
        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "search view clicked");
                mSearchView.setIconified(false);
                isTyping = true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));
                mMusicResultsList.setVisibility(View.GONE);
                mSearchResultsAdapter.clearData();
                mSearchView.clearFocus();
                return true;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (isTyping) {
                    mMusicResultsList.setVisibility(View.VISIBLE);
                    isTyping = false;
                }

                if (!newText.equals("")) {
                    mSearchContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorPrimaryDark));

                    mainHandler.removeCallbacks(runnable);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "text changed trigger: " + newText);
                            mActionListener.search(newText);
                        }
                    };
                    mainHandler.postDelayed(runnable, 150);
                }

                return true;
            }
        });

        // Setup search results list
        mSearchResultsAdapter = new SearchResultsAdapter(getContext(), new SearchResultsAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(View itemView, Track item) {
                Log.d(TAG, "result item selected");
                SimpleTrack simpleTrack = new SimpleTrack(item);
                mMusicResultsList.setVisibility(View.GONE);
                mMusicDatabaseAccess.push(simpleTrack);
                mSearchView.setQuery("", false);
                mSearchContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.transparent));
                mSearchResultsAdapter.clearData();
                isTyping = true;
            }
        });

        mMusicResultsList.setHasFixedSize(true);
        mMusicResultsList.setLayoutManager(mResultsLayoutManager);
        mMusicResultsList.setAdapter(mSearchResultsAdapter);
        mMusicResultsList.addOnScrollListener(mScrollListener);

        mQueueLayoutManager.setReverseLayout(true);
        mMusicQueueList.setLayoutManager(mQueueLayoutManager);
        mQueueMusicAdapter = new MusicQueueAdapter(getContext(), this);
        mMusicQueueList.setAdapter(mQueueMusicAdapter);

        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "music frag create");

        PreferenceUtility.initialize(getContext());
        mSpotifyAccessToken = PreferenceUtility.getPreference(PreferenceUtility.SPOTIFY_TOKEN);

        mMusicDatabaseAccess = new FirebaseEventBus.MusicDatabaseAccess(getContext(), this);
        mMusicDatabaseAccess.addChildListener();

        mActionListener = new SearchPresenter(getContext(), this);
        mActionListener.init(mSpotifyAccessToken);

        // If Activity was recreated wit active search restore it
        if (savedInstanceState != null) {
            String currentQuery = savedInstanceState.getString(KEY_CURRENT_QUERY);
            mActionListener.search(currentQuery);
        }
    }

    @Override
    public void reset() {
        mScrollListener.reset();
        mSearchResultsAdapter.clearData();
    }

    @Override
    public void addData(List<Track> items) {
        mSearchResultsAdapter.addData(items);
    }


    @Override
    public void enqueue(SimpleTrack simpleTrack) {
        mQueueMusicAdapter.enqueue(simpleTrack);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActionListener.pause();

    }

    @Override
    public void onResume() {
        super.onResume();
        mActionListener.resume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActionListener.getCurrentQuery() != null) {
            outState.putString(KEY_CURRENT_QUERY, mActionListener.getCurrentQuery());
        }
    }
}
