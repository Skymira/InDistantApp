package com.example.indistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.indistant.adapters.AdapterPosts;
import com.example.indistant.models.ModelPost;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ForumActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        firebaseAuth = FirebaseAuth.getInstance();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Forum");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF018786"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);


        //recycler view
        recyclerView = findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // show newest posts first

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);


        // init post list
        postList = new ArrayList<>();

        loadPosts();

    }

    private void loadPosts() {
        // Path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        // get all data from this ref

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    postList.add(modelPost);

                    //adapter
                    adapterPosts = new AdapterPosts(ForumActivity.this, postList);
                    //set adapter
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // in case of error
                Toast.makeText(ForumActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(String searchQuery){
        // Path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        // get all data from this ref

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }



                    //adapter
                    adapterPosts = new AdapterPosts(ForumActivity.this, postList);
                    //set adapter
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // in case of error
                Toast.makeText(ForumActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_forum, menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
                    loadPosts();
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if ( id == R.id.search){
            // later
        }
        if(id == R.id.action_add_post){
            startActivity(new Intent(ForumActivity.this, AddPostActivity.class));
        }
        if (id == R.id.settings) {
            startActivity(new Intent(ForumActivity.this, SettingsActivity.class));
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
            startActivity(new Intent(ForumActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}