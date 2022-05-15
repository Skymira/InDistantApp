package com.example.indistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat {



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getActivity().setContentView(R.layout.fragment_settings);

        super.onCreate(savedInstanceState);


    }





    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference accountPreference = findPreference("account");
        SwitchPreferenceCompat switchTheme = findPreference("switchTheme");
        Preference language = findPreference("language");

        language.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
               // displayLanguage();
                Locale[] locales = Locale.getAvailableLocales();
                ArrayList<String> languages = new ArrayList<String>();
                for (Locale locale : locales) {
                    String language = locale.getDisplayName();
                    if (language.trim().length()>0 && !languages.contains(language)) {
                        languages.add(language);
                    }
                }
                Collections.sort(languages);


                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setItems(languages.toArray
                                (new String[languages.size()]),
                        new DialogInterface.OnClickListener(){


                            public void onClick(DialogInterface dialog,int which){
                                language.setSummary(languages.get(which).toString());

                            }

                        });
                builder.create();
                builder.show();


                return true;
            }
        });

        accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, new AccountFragment())
                        .commit();
                return true;
            }
        });
    }

    public void ToggleDarkMode() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);


        if (isDarkModeOn) {

            // if dark mode is on it
            // will turn it off
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
            // it will set isDarkModeOn
            // boolean to false
            editor.putBoolean(
                    "isDarkModeOn", false);
            editor.apply();

        }


        if(!isDarkModeOn){
            // if dark mode is off
            // it will turn it on
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);

            // it will set isDarkModeOn
            // boolean to true
            editor.putBoolean(
                    "isDarkModeOn", true);
            editor.apply();

        }
    }





}