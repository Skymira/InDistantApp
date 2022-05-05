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
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.indistant.adapters.AdapterUsers;
import com.example.indistant.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

    public class UsersFragment extends Fragment {

        RecyclerView recyclerView;
        AdapterUsers adapterUsers;
        List<ModelUsers> userList;

        FirebaseAuth mAuth;

        public UsersFragment() {
            // Required empty public constructor
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view =  inflater.inflate(R.layout.fragment_users, container, false);

            mAuth = FirebaseAuth.getInstance();

            recyclerView = view.findViewById(R.id.users_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            // Init user list
            userList = new ArrayList<>();

            // get all users
            getAllUsers();


            return view;
        }

        private void getAllUsers() {
            // get current user
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            // get path of database named "Users" containing users info
            DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
            //get all data from path
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                        // Get all users except currently signed in user
                        assert fUser != null;
                        if(!modelUsers.getUid().equals(fUser.getUid())){
                            userList.add(modelUsers);
                        }

                        //adapter
                        adapterUsers = new AdapterUsers(getActivity(), userList);
                        //set adapter to recycler view
                        recyclerView.setAdapter(adapterUsers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void searchUsers(String query) {
            // get current user
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            // get path of database named "Users" containing users info
            DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
            //get all data from path
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                        // Get all searched users except currently signed in user
                        if(!modelUsers.getUid().equals(fUser.getUid())){


                            if(modelUsers.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                                    modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())) {
                                userList.add(modelUsers);
                            }

                        }

                        //adapter
                        adapterUsers = new AdapterUsers(getActivity(), userList);
                        //refresh adapter
                        adapterUsers.notifyDataSetChanged();
                        //set adapter to recycler view
                        recyclerView.setAdapter(adapterUsers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        private void checkUserStatus() {
            FirebaseUser mUser = mAuth.getCurrentUser();
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

            // SearchView
            MenuItem item = menu.findItem(R.id.search);
            SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

            // Search listener
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    // called when user press search button from keyboard
                    // if search query is not empty then search

                    if(!TextUtils.isEmpty(s.trim())){
                        // search text contains text, search it
                        searchUsers(s);
                    }
                    else {
                        // search text empty, get all users
                        getAllUsers();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // called when user press any single letter
                    if(!TextUtils.isEmpty(s.trim())){
                        // search text contains text, search it
                        searchUsers(s);
                    }
                    else {
                        // search text empty, get all users
                        getAllUsers();
                    }
                    return false;
                }
            });

            super.onCreateOptionsMenu(menu, inflater);
        }



        // Handle menu items on click


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            // Get item id
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                mAuth.signOut();
                checkUserStatus();
            }
            if (id == R.id.settings) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }
