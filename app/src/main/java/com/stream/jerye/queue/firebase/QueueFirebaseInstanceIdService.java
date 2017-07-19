package com.stream.jerye.queue.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stream.jerye.queue.PreferenceUtility;

/**
 * Created by jerye on 7/1/2017.
 */

public class QueueFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Registration Token", refreshedToken);

        PreferenceUtility.initialize(getApplicationContext());
        PreferenceUtility.setPreference(PreferenceUtility.FIREBASE_REGISTRATION_TOKEN, refreshedToken);

    }
}
