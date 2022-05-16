package com.example.indistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class NotificationFragment extends PreferenceFragmentCompat {

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    SwitchPreferenceCompat postNotification;

    //Constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";


    public NotificationFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        getActivity().setContentView(R.layout.fragment_notification);
        super.onCreate(savedInstanceState);



        sp = getActivity().getSharedPreferences("Notification_Sp", Context.MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION, false);
        if(isPostEnabled) {
            postNotification.setChecked(true);
        }
        else {
            postNotification.setChecked(false);
        }

    }


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);
        postNotification = findPreference("postNotifications");
        postNotification.setChecked(false);

       postNotification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
           @Override
           public boolean onPreferenceClick(@NonNull Preference preference) {

               // Edit switch state
               editor = sp.edit();
               editor.putBoolean(""+TOPIC_POST_NOTIFICATION, postNotification.isChecked());
               editor.apply();
               if(postNotification.isChecked()){
                   //Toast.makeText(getActivity(), "WORKING", Toast.LENGTH_SHORT).show();
                   subscribePostNotification();
               }
               else {
                  // Toast.makeText(getActivity(), "NOT WORKING", Toast.LENGTH_SHORT).show();
                   unsubscribePostNotification();
               }
               return true;
           }
       });

    }
    

    private void unsubscribePostNotification() {
        // Disable notifications
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notifications";
                        if(!task.isSuccessful()){
                            msg = "UnSubscription failed!";
                        }
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        // Enable notifications
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notifications";
                        if(!task.isSuccessful()){
                            msg = "Subscription failed!";
                        }
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}