package com.example.indistant;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    // Views
    EditText mEmailEt, mPasswordEt;
    TextView mRecoverPassTv, signUpFragment;
    Button mLoginBtn;
    SignInButton mGoogleLoginButton;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF018786"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sing In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        mAuth = FirebaseAuth.getInstance();

        // Init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        signUpFragment = findViewById(R.id.signUpFragment);
        mRecoverPassTv = findViewById(R.id.mRecoverPassTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mGoogleLoginButton = findViewById(R.id.googleLogin);


        //getSupportFragmentManager().beginTransaction().add(R.id.container1, new SignUpFragment()).commit();

        signUpFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.signUpFragment){
                    // getSupportFragmentManager().beginTransaction().replace(R.id.container1, new SignUpFragment()).commit();
                    SignUpFragment fragment1 = new SignUpFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.container1, fragment1, "");
                    ft1.commit();
                }
            }
        });



        // Login Button Click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input data
                String email = mEmailEt.getText().toString();
                String password = mPasswordEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.requestFocus();
                }
                else {
                    loginUser(email, password);
                }
            }
        });

        //Recover Pass TextView Click
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        // Handle Google Login btn Click
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Google Login Process
                Intent singInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(singInIntent, RC_SIGN_IN);
            }
        });



        // init progress dialog
        progressDialog = new ProgressDialog(this);

    }







        private void showRecoverPasswordDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Password Recovery");
            // Set LinearLayout for our AlertDialog
            LinearLayout linearLayout = new LinearLayout(this);

            // Views to set in dialog
            EditText emailEt = new EditText(this);
            emailEt.setHint("Email");
            emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            emailEt.setMinEms(16);

            linearLayout.addView(emailEt);
            linearLayout.setPadding(60,60,60,30);

            builder.setView(linearLayout);

            // Button Recover
            builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String email = emailEt.getText().toString().trim();
                    beginRecovery(email);
                }
            });

            // Button Cancel
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Dismiss Dialog
                    dialogInterface.dismiss();
                }
            });

            // Show Dialog
            builder.create().show();
        }

        private void beginRecovery(String email) {
            progressDialog.setMessage("Sending email...");
            progressDialog.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Verification email had been sent", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Email could not be sent", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void loginUser(String email, String password) {
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        FirebaseUser mUser = mAuth.getCurrentUser();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                       finish();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthwithGoogle(account);
                } catch (ApiException e) {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        private void firebaseAuthwithGoogle(GoogleSignInAccount acct) {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        FirebaseUser mUser = mAuth.getCurrentUser();

                        if(task.getResult().getAdditionalUserInfo().isNewUser()) {
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
                            hashMap.put("country", "");
                            hashMap.put("distance", "");
                            hashMap.put("cover","");
                            hashMap.put("name","");
                            hashMap.put("birthday","");
                            hashMap.put("gender","");

                            // Firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app");

                            //Store UserData create first tree in RTDB
                            DatabaseReference reference = database.getReference("Users");
                            // Put data from hashmap to Users
                            reference.child(uid).setValue(hashMap);
                        }

                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                       finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        // Inflate menu options

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_outside,menu);
        return super.onCreateOptionsMenu(menu);
    }


    // Handle menu items on click


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            // Get item id
            int id = item.getItemId();

            if (id == R.id.settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void ToggleDarkMode() {
            SharedPreferences sharedPreferences
                    = getSharedPreferences(
                    "sharedPrefs", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor
                    = sharedPreferences.edit();
            final boolean isDarkModeOn
                    = sharedPreferences
                    .getBoolean(
                            "isDarkModeOn", false);


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

            } else {

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













