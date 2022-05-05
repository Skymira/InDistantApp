package com.example.indistant;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


    public class ProfileFragment extends Fragment {

        // Firebase
        FirebaseAuth firebaseAuth;
        FirebaseUser user;
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;

        // Storage
        StorageReference storageReference;

        // Views
        ImageView avatarIv, coverIv;
        TextView usernameTv, countriesTv, nationalityTv, nameTv, birthdayTv, genderTv;

        public ProfileFragment() {
            // Required empty public constructor
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment


            View view =  inflater.inflate(R.layout.fragment_profile, container, false);

            // Init firebase
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            firebaseDatabase = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
            databaseReference = firebaseDatabase.getReference("Users");
            storageReference = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReference();


            // Init views
            avatarIv = view.findViewById(R.id.avatarIv);
            coverIv = view.findViewById(R.id.coverIv);
            usernameTv = view.findViewById(R.id.usernameTv);
            nameTv = view.findViewById(R.id.nameTv);
            countriesTv = view.findViewById(R.id.countriesTv);
            nationalityTv = view.findViewById(R.id.nationalityTv);
            genderTv = view.findViewById(R.id.genderTv);
            birthdayTv = view.findViewById(R.id.birthdayTv);


            Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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
                        String nationality = ""+ds.child("nationality").getValue();

                        // Set data
                        nameTv.setText(name);
                        usernameTv.setText(username);
                        countriesTv.setText(myCountry);
                        genderTv.setText(gender);
                        birthdayTv.setText(birthday);
                        nationalityTv.setText(nationality);

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




            return view;
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
    }
