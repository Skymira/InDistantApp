package com.example.indistant;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.indistant.notifications.Data;
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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    Uri image_uri = null;
    ProgressDialog pd;



    private static final  int CAMERA_REQUEST_CODE = 100;
    private static final  int STORAGE_REQUEST_CODE = 200;
    private static final  int IMAGE_PICK_GALLERY_CODE = 300;
    private static final  int IMAGE_PICK_CAMERA_CODE = 400;


    String[] cameraPermissions;
    String[] storagePermissions;
    // Views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    // user info
    String username, country,email, uid, dp;

    // info of post to be edited
    String  editTitle, editDescription, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FF018786"));
        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);


        //Init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        pd = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // init
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);



        // get data throught intent from previous activitie's adapter
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        // validate if we came here to update post i.e came from AdapterPost
        if(isUpdateKey.equals("editPost")){
            // update
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }
        else {
            // add
            actionBar.setTitle("Add New Post");
            uploadBtn.setText("Publish");

        }

        actionBar.setSubtitle(username);

        // get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    username = ""+ds.child("username").getValue();
                    dp = ""+ds.child("image").getValue();
                    country = ""+ds.child("country").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        // get image from camera or gallery on click

        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show image pick dialog
                showImagePickDialog();
            }
        });


        //Upload button click listener

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data(title, desc) from EditTexts
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Please enter the title !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Please enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editPost")){
                   beginUpdate(title, description, editPostId);

                }
                else {
                    uploadData(title, description);
                }


            }
        });
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();

        if(!editImage.equals("noImage")){
            updateWasWithImage(title, description, editPostId);
        }
        else if(imageIv.getDrawable() != null){
            updateWithNowImage(title, description, editPostId);
        }
        else {
            // without image
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("uUsername",username);
        hashMap.put("uEmail",email);
        hashMap.put("uDp",dp);
        hashMap.put("pCountry",country);
        hashMap.put("pTitle",title);
        hashMap.put("pDescription",description);
        hashMap.put("pImage","noImage");
        hashMap.put("pLikes", "0");
        hashMap.put("pComments","0");

        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddPostActivity.this, ForumActivity.class);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateWithNowImage(String title, String description, String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        //get image from imageview
        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReference().child(filePathAndName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded get its url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                    //uri is recieved , upload to firebase database

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid",uid);
                    hashMap.put("uUsername",username);
                    hashMap.put("uEmail",email);
                    hashMap.put("uDp",dp);
                    hashMap.put("pCountry",country);
                    hashMap.put("pTitle",title);
                    hashMap.put("pDescription",description);
                    hashMap.put("pImage",downloadUri);
                    hashMap.put("pLikes", "0");
                    hashMap.put("pComments","0");

                    DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                    ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddPostActivity.this, ForumActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // image not uploaded
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateWasWithImage(String title, String description, String editPostId) {
        StorageReference mPictureRef = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //image deleted, upload new image
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/"+"post_"+timeStamp;

                //get image from imageview
                Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReference().child(filePathAndName);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            //uri is recieved , upload to firebase database

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid",uid);
                            hashMap.put("uUsername",username);
                            hashMap.put("uEmail",email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pCountry",country);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescription",description);
                            hashMap.put("pImage",downloadUri);
                            hashMap.put("pLikes", "0");
                            hashMap.put("pComments","0");

                            DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                            ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AddPostActivity.this, ForumActivity.class);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // image not uploaded
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
        // Get detail og post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    // Get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescription").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    // Set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set image
                    if(!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch(Exception e ){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String title, String description) {
        pd.setMessage("Publishing post...");
        pd.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_"+ timeStamp;

        if(imageIv.getDrawable() != null){
            //get image from imageview
           Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();


            // post with image

            StorageReference ref = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com").getReference().child(filePathAndName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid",uid);
                        hashMap.put("uUsername",username);
                        hashMap.put("uEmail",email);
                        hashMap.put("uDp",dp);
                        hashMap.put("pId",timeStamp);
                        hashMap.put("pTitle",title);
                        hashMap.put("pDescription",description);
                        hashMap.put("pImage",downloadUri);
                        hashMap.put("pTime",timeStamp);
                        hashMap.put("pCountry",country);
                        hashMap.put("pLikes", "0");
                        hashMap.put("pComments","0");

                        // path to store post data
                        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
                        // put data in this ref
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //added
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post published :)", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddPostActivity.this, ForumActivity.class);
                                startActivity(intent);
                                //reset views
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_uri = null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
                // post without image

            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uUsername",username);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescription",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);
            hashMap.put("pCountry",country);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments","0");

            // path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Posts");
            // put data in this ref
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //added
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post published :)", Toast.LENGTH_SHORT).show();
                    //reset views
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};

        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options to dialog

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //item click handle
                if(i == 0){
                    //camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if( i == 1){
                    //gallery cicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        // create and show dialog
        builder.create().show();
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        // Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        // Put image uri
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
                        Toast.makeText(AddPostActivity.this, "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddPostActivity.this , "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK) {

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                // Image is picked from gallery, get uri of image
                image_uri = data.getData();

               imageIv.setImageURI(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // Image is picked from camera, get uri of image
                imageIv.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_forum, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
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
            startActivity(new Intent(AddPostActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser mUser = firebaseAuth.getCurrentUser();
        if (mUser != null) {
            // If user is singed in
            email = mUser.getEmail();
            uid = mUser.getUid();

        } else {
            // If user is singed out
            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
            finish();
        }
    }
}