package com.example.indistant;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

    public class HomeFragment extends Fragment {

        FirebaseAuth firebaseAuth;

        Button forum;

        public HomeFragment() {
            // Required empty public constructor
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view =  inflater.inflate(R.layout.fragment_home, container, false);

            firebaseAuth = FirebaseAuth.getInstance();

            forum = view.findViewById(R.id.forum);

            forum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ForumActivity.class);
                    startActivity(intent);
                }
            });


            return view;
        }



        private void checkUserStatus() {
            FirebaseUser mUser = firebaseAuth.getCurrentUser();
            if (mUser != null) {
                // If user is singed in

            } else {
                // If user is singed out
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        }


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            // To show menu option in fragment
            setHasOptionsMenu(true);


            super.onCreate(savedInstanceState);
        }

        // Inflate menu options

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // inflating menu
            inflater.inflate(R.menu.menu_main, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        // Handle menu items on click


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            // Get item id
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                firebaseAuth.signOut();
                checkUserStatus();
            }
            if (id == R.id.settings) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }
