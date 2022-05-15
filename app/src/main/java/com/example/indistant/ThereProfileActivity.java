package com.example.indistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ThereProfileActivity extends AppCompatActivity {

    String uid;
    FirebaseAuth firebaseAuth;
    // Views
    ImageView avatarIv, coverIv;
    TextView usernameTv, countriesTv, nameTv, birthdayTv, genderTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#008577"));

            // Set BackgroundDrawable
            actionBar.setBackgroundDrawable(colorDrawable);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        // Init views
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        usernameTv = findViewById(R.id.usernameTv);
        nameTv = findViewById(R.id.nameTv);
        countriesTv = findViewById(R.id.countriesTv);
        genderTv = findViewById(R.id.genderTv);
        birthdayTv = findViewById(R.id.birthdayTv);

        Query query = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check untill required data get
                for(DataSnapshot ds : snapshot.getChildren()) {


                    // Get data
                    String username =""+ds.child("username").getValue();
                    String name = ""+ds.child("name").getValue();
                    String myCountry =""+ds.child("country").getValue();
                    String gender = ""+ds.child("gender").getValue();
                    String birthday = ""+ds.child("birthday").getValue();
                    String image =""+ds.child("image").getValue();
                    String cover =""+ds.child("cover").getValue();
                    String email = ""+ds.child("email").getValue();
                    String uid = ""+ds.child("uid").getValue();

                    // Set data
                    birthdayTv.setText(birthday);
                    usernameTv.setText(username);
                    nameTv.setText(name);
                    countriesTv.setText(myCountry);
                    genderTv.setText(gender);



                    try {
                        // Load image if it exists
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        // Load add image drawable If it doesnt exist
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv);
                    }
                    try {
                        // Load image if it exists
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        // Load add image drawable If it doesnt exist
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        checkUserStatus();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id == R.id.settings) {
            startActivity(new Intent(ThereProfileActivity.this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser mUser = firebaseAuth.getCurrentUser();
        if (mUser != null) {
            // If user is singed in

        } else {
            // If user is singed out
            startActivity(new Intent(ThereProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}