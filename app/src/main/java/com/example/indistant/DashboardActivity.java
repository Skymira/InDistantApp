package com.example.indistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {


        // Firebase Auth
        FirebaseAuth mAuth;

        ActionBar actionBar;
        // Views


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

            actionBar = getSupportActionBar();
            actionBar.setTitle("Home");

            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#008577"));
            actionBar.setBackgroundDrawable(colorDrawable);


            // Init
            mAuth = FirebaseAuth.getInstance();

            // Bottom nav
            BottomNavigationView navigationView = findViewById(R.id.navigation);
            navigationView.setOnNavigationItemSelectedListener(selectedListener);

            if(savedInstanceState == null){
                getSupportFragmentManager().
                        beginTransaction().replace(R.id.content,new HomeFragment()).commit();
            }

            // Init views

        }

        private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        // Handle item Clicks
                        switch(item.getItemId()) {
                            case R.id.nav_home:
                                // Home Fragment transaction
                                actionBar.setTitle("Home");
                                HomeFragment fragment1 = new HomeFragment();
                                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                                ft1.replace(R.id.content, fragment1, "");
                                ft1.commit();
                                return true;
                            case R.id.nav_profile:
                                // Profile Fragment transaction
                                actionBar.setTitle("Profile");
                                ProfileFragment fragment2 = new ProfileFragment();
                                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                                ft2.replace(R.id.content, fragment2, "");
                                ft2.commit();
                                return true;
                            case R.id.nav_users:
                                // Users Fragment transaction
                                actionBar.setTitle("Users");
                                UsersFragment fragment3 = new UsersFragment();
                                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                                ft3.replace(R.id.content, fragment3, "");
                                ft3.commit();
                                return true;
                            case R.id.nav_chat:
                                // Users Fragment transaction
                                actionBar.setTitle("Chats");
                                ChatListFragment fragment4 = new ChatListFragment();
                                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                                ft4.replace(R.id.content, fragment4, "");
                                ft4.commit();
                                return true;
                        }
                        return false;
                    }
                };

        private void checkUserStatus() {
            FirebaseUser mUser = mAuth.getCurrentUser();
            if (mUser != null) {
                // If user is singed in

            } else {
                // If user is singed out
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                finish();
            }
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            finish();
        }

        @Override
        protected void onStart() {
            checkUserStatus();
            super.onStart();
        }


        @Override
        public boolean onSupportNavigateUp() {
            onBackPressed(); // go previous activity
            return super.onSupportNavigateUp();
        }



    }
