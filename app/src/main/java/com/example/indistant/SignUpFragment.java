package com.example.indistant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignUpFragment extends Fragment {

    TextView loginTv;

    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;

    // Progressbar to display while registering user
    ProgressDialog progressDialog;

    // Firebase Auth

    private FirebaseAuth mAuth;


    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Sign up");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#008577"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

         //Enable back button
          // actionBar.setDisplayHomeAsUpEnabled(true);
         // actionBar.setDisplayShowHomeEnabled(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        loginTv = view.findViewById(R.id.loginFragment2);



        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.loginFragment2){
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });


        // Init
        mEmailEt = view.findViewById(R.id.emailEt);
        mPasswordEt = view.findViewById(R.id.passwordEt);
        mRegisterBtn = view.findViewById(R.id.registerBtn);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Registering User...");

        // Init Auth
        mAuth = FirebaseAuth.getInstance();


        // Handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input Email, Password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();

                // Validate them
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()){
                    // Set Error and refocus
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.requestFocus();
                }
                else if (password.length() < 6) {
                    mPasswordEt.setError("Password must be at least 6 characters");
                    mPasswordEt.requestFocus();
                }
                else {
                    // Register the User
                    registerUser(email, password);
                }

            }
        });

        // Handle login textview
        return  view;
    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    FirebaseUser mUser = mAuth.getCurrentUser();

                    String email = mUser.getEmail();
                    String uid = mUser.getUid();
                    // Store info in Hashmap
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email", email);
                    hashMap.put("uid", uid);
                    hashMap.put("username", "");
                    hashMap.put("status","online");
                    hashMap.put("typingTo","noOne");
                    hashMap.put("image", "");
                    hashMap.put("distance", "");
                    hashMap.put("cover", "");
                    hashMap.put("name","");
                    hashMap.put("birthday","");
                    hashMap.put("gender","");
                    hashMap.put("partnersCountry","");
                    hashMap.put("country","");

                    // Firebase database instance
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app");

                    //Store UserData create first tree in RTDB
                    DatabaseReference reference = database.getReference("Users");
                    // Put data from hashmap to Users
                    reference.child(uid).setValue(hashMap);

                    Toast.makeText(getActivity(), "Registered...\n"+mUser.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@androidx.annotation.NonNull Menu menu, @androidx.annotation.NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_outside, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
       int id = 0;

       if( id == R.id.settings){
           startActivity(new Intent(getActivity(), SettingsActivity.class));
       }


        return super.onOptionsItemSelected(item);
    }
}







