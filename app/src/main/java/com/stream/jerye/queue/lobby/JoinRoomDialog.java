package com.stream.jerye.queue.lobby;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.firebase.FirebaseEventBus;
import com.stream.jerye.queue.room.RoomActivity;
import com.stream.jerye.queue.widget.QueueWidgetProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jerye on 7/1/2017.
 */

public class JoinRoomDialog extends DialogFragment implements FirebaseEventBus.FirebaseRoomInfoHandler {
    @BindView(R.id.dialog_join_room_title)
    EditText roomTitleEditText;
    @BindView(R.id.dialog_join_room_password)
    EditText roomPasswordEditText;

    private String mTitleAttempt;
    private String mPasswordAttempt;
    private List<Room> listOfRooms = new ArrayList<>();


    @Override
    public void getRooms(Room room) {
        listOfRooms.add(room);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.join_room_dialog, null);
        ButterKnife.bind(this, dialogView);
        PreferenceUtility.initialize(getActivity());

        FirebaseEventBus.RoomDatabaseAccess mRoomAccessDatabase = new FirebaseEventBus.RoomDatabaseAccess(getActivity(), this);
        mRoomAccessDatabase.getRooms();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        roomTitleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });
        roomPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        });
        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_join, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });


        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button negativeButton = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean enteredRoomFlag = false;
                    if(!roomTitleEditText.getText().toString().equals("")){
                        mTitleAttempt = roomTitleEditText.getText().toString();
                    }else{
                        Toast.makeText(getActivity(), getString(R.string.dialog_room_title_blank), Toast.LENGTH_SHORT).show();
                    }
                    mPasswordAttempt = roomPasswordEditText.getText().toString();

                    for (Room room : listOfRooms) {

                        if (mTitleAttempt.equals(room.getTitle()) && (mPasswordAttempt.equals(room.getPassword()) || room.getPassword() == null)) {
                            PreferenceUtility.setPreference(PreferenceUtility.ROOM_KEY,room.getRoomKey());
                            Log.d("Dialog", "room key: " + room.getRoomKey());

                            PreferenceUtility.setPreference(PreferenceUtility.ROOM_TITLE, mTitleAttempt);
                            PreferenceUtility.setPreference(PreferenceUtility.ROOM_PASSWORD, mPasswordAttempt);

                            Intent intent = new Intent(getActivity(), RoomActivity.class)
                                    .setAction(LobbyActivity.ACTION_NEW_USER)
                                    .putExtra("room title", mTitleAttempt)
                                    .putExtra("room password", mPasswordAttempt);
                            startActivity(intent);
                            alertDialog.dismiss();
                            enteredRoomFlag = true;

                            Intent widgetIntent = new Intent(QueueWidgetProvider.ACTION_WIDGET_CONNECTION);
                            getActivity().sendBroadcast(widgetIntent);
                            break;
                        }

                    }

                    if(!enteredRoomFlag){
                        Toast.makeText(getActivity(), getString(R.string.dialog_incorrect_room_pass), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

}
