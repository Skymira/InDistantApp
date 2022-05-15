package com.example.indistant;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;


public class AccountFragment extends PreferenceFragmentCompat {


    FirebaseAuth firebaseAuth1;
    FirebaseUser user1;
    FirebaseDatabase firebaseDatabase1;
    DatabaseReference databaseReference1;

    // Storage
    StorageReference storageReference;
    //Path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    ProgressDialog pd;

    // Permissions constans
    private static final  int CAMERA_REQUEST_CODE = 100;
    private static final  int STORAGE_REQUEST_CODE = 200;
    private static final  int IMAGE_PICK_GALLERY_CODE = 300;
    private static final  int IMAGE_PICK_CAMERA_CODE = 400;

    // Arrays of permissions to be requested
    String[] cameraPermissions;
    String[] storagePermissions;

    // Uri of picked image
    Uri image_uri;

    // for checking profile or cover photo
    String profileOrCoverPhoto;



    String uid;
    private ImageView avatarIv2;
    private ImageView coverIv2;


    Button avatarUpload;
    private int mDay, mMonth, mYear;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getActivity().setContentView(R.layout.fragment_account);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        firebaseAuth1 = FirebaseAuth.getInstance();
        user1 = firebaseAuth1.getCurrentUser();
        firebaseDatabase1 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference1 = firebaseDatabase1.getReference("Users");
        storageReference = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReference();
        Intent intent = getActivity().getIntent();
        uid = intent.getStringExtra("uid");



        //Init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(getActivity());
        avatarUpload = getActivity().findViewById(R.id.uploadProfileImage);
        avatarIv2 = getActivity().findViewById(R.id.avatarIv2);
        coverIv2 = getActivity().findViewById(R.id.coverIv2);
        setPreferencesFromResource(R.xml.account_preferences, rootKey);
        Preference editName = findPreference("name");
        Preference editUsername = findPreference("username");
        Preference editBirthday = findPreference("birthday");
        Preference editGender = findPreference("gender");
        Preference editCountry = findPreference("country");
        Preference editEmail = findPreference("email");





        editEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return true;
            }
        });





        avatarUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });




        editBirthday.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FirebaseAuth firebaseAuth;
                FirebaseUser user;
                FirebaseDatabase firebaseDatabase;
                DatabaseReference databaseReference;

                firebaseAuth = FirebaseAuth.getInstance();
                user = firebaseAuth.getCurrentUser();
                firebaseDatabase = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
                databaseReference = firebaseDatabase.getReference("Users");
                //  ShowEdits("birthday");
                final Calendar cal = Calendar.getInstance(Locale.US);
                mDay = cal.get(Calendar.DAY_OF_MONTH);
                mMonth = cal.get(Calendar.MONTH);
                mYear = cal.get(Calendar.YEAR);




                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        editBirthday.setTitle(day+"/"+month+"/"+year);
                        String value = editBirthday.toString().trim();
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("birthday", value);
                        databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }, mYear, mMonth, mDay);

                datePickerDialog.show();






                return true;


            }

        });

        editCountry.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


            @Override
            public boolean onPreferenceClick(Preference preference) {
                //ShowEdits("country");
                displayCountries();




                return true;
            }


        });

        editGender.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShowEdits("gender");
                return true;
            }
        });




        editName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShowEdits("name");
                return true;
            }
        });
        editUsername.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShowEdits("username");
                return true;
            }
        });

        Query query = databaseReference1.orderByChild("email").equalTo(user1.getEmail());
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

                    if(birthday.isEmpty()){
                        editBirthday.setTitle("Birthday");
                    }
                    else {
                        editBirthday.setTitle(birthday);
                    }
                    if(name.isEmpty()){
                        editName.setTitle("Name and Surname");
                    }
                    else {
                        editName.setTitle(name);
                    }
                    if(username.isEmpty()){
                        editUsername.setTitle("Username");
                    }
                    else {
                        editUsername.setTitle(username);
                    }
                    if(gender.isEmpty()){
                        editGender.setTitle("Gender");
                    }
                    else {
                        editGender.setTitle(gender);
                    }
                    if(myCountry.isEmpty()){
                        editCountry.setTitle("Country");
                    }
                    else {
                        editCountry.setTitle(myCountry);
                    }
                    editEmail.setTitle(email);

                    try {
                        // Load image if it exists
                        Picasso.get().load(image).into(avatarIv2);
                    }
                    catch (Exception e){
                        // Load add image drawable If it doesnt exist
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarIv2);
                    }
                    try {
                        // Load image if it exists
                        Picasso.get().load(cover).into(coverIv2);
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




    }

    private void ShowEdits(String key) {
        FirebaseAuth firebaseAuth;
        FirebaseUser user;
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference("Users");
        //custom dialog
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        // set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(60,10,60,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter"+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // Create and show dialog
        builder.create().show();
    }
    private void showRecoverPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Password Recovery");
        // Set LinearLayout for our AlertDialog
        LinearLayout linearLayout = new LinearLayout(getActivity());

        // Views to set in dialog
        EditText emailEt = new EditText(getActivity());
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
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getActivity());


        progressDialog.setMessage("Sending email...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@android.support.annotation.NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Verification email had been sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Email could not be sent", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@android.support.annotation.NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }







    // ghgggggggggggggggggg
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
       return result && result1;





    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {
        // Show dialog containing options Camera and Gallery to pick the image
        String options[] = {"Camera", "Gallery"};
        // Alert Diaolog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle dialog item clicks
                if(i == 0) {
                    // Camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }

                }
                else if(i == 1){
                    // Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }
            }
        });
        // Create and show dialog
        builder.create().show();
    }

    private void showUsernameCountryUpdateDialog(String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        // set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edit text
                final String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference1.child(user1.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter"+key,Toast.LENGTH_SHORT).show();
                }
                if(key.equals("username")){
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                    Query query = ref.orderByChild("uid").equalTo(uid);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String child = ds.getKey();
                                snapshot.getRef().child(child).child("uUsername").setValue(value);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String child = ds.getKey();
                                if(snapshot.child(child).hasChild("Comments")){
                                    String child1 = ""+snapshot.child(child).getKey();
                                    Query child2 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                    child2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for( DataSnapshot ds : snapshot.getChildren()){
                                                String child = ds.getKey();
                                                snapshot.getRef().child(child).child("uUsername").setValue(value);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
                else if(key.equals("country")){
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                    Query query = ref.orderByChild("uid").equalTo(uid);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String child = ds.getKey();
                                snapshot.getRef().child(child).child("pCountry").setValue(value);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String child = ds.getKey();
                                if(snapshot.child(child).hasChild("Comments")){
                                    String child1 = ""+snapshot.child(child).getKey();
                                    Query child2 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                    child2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for( DataSnapshot ds : snapshot.getChildren()){
                                                String child = ds.getKey();
                                                snapshot.getRef().child(child).child("pCountry").setValue(value);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });





        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // Create and show dialog
        builder.create().show();

    }

    private void showEditProfileDialog() {
        String options[] = {"Edit Profile Picture", "Edit Cover Photo"};
        // Alert Diaolog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle dialog item clicks
                if(i == 0) {
                    // Edit Profile Clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if(i == 1){
                    // Edit Cover clicked
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
            }
        });
        // Create and show dialog
        builder.create().show();
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickFromGallery();
                    pickFromCamera();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        // Permissions enabled
                        pickFromCamera();
                    } else {
                        // permissions denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        // Permissions enabled
                        pickFromGallery();
                    } else {
                        // permissions denied
                        Toast.makeText(getActivity() , "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }


    }
*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK) {

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                // Image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // Image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {
        // Show progress
        pd.show();

        // Path and name of image to be stored in firebase storage




        String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+user1.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if(uriTask.isSuccessful()){
                    //image uploaded
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto, downloadUri.toString());

                    databaseReference1.child(user1.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error updating image", Toast.LENGTH_SHORT).show();
                        }
                    });


                    if(profileOrCoverPhoto.equals("image")){
                        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for( DataSnapshot ds : snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                }
                else {
                    // error
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void pickFromCamera() {
        // Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        // Put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        // Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void displayCountries(){

        FirebaseAuth firebaseAuth;
        FirebaseUser user;
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference("Users");

        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length()>0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries);


        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setItems(countries.toArray
                        (new String[countries.size()]),
                new DialogInterface.OnClickListener(){


                    public void onClick(DialogInterface dialog,int which){


                        HashMap<String, Object> result = new HashMap<>();
                        result.put("country",countries.get(which).toString());



                        databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });



                        switch(which){


                        }
                    }

                });
        builder.create();
        builder.show();
    }








    }



