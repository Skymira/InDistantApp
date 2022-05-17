package com.example.indistant;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.indistant.adapters.AdapterChat;
import com.example.indistant.models.ModelChat;
import com.example.indistant.models.ModelUsers;
import com.example.indistant.notifications.Data;
import com.example.indistant.notifications.Sender;
import com.example.indistant.notifications.Token;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

        // Views from xml
        Toolbar toolbar;
        RecyclerView recyclerView;
        ImageView profileIv;
        TextView nameTv, statusTv;
        EditText messageEt;
        ImageButton sendBtn, attachBtn;

        FirebaseAuth mAuth;

        FirebaseDatabase firebaseDatabase;
        DatabaseReference usersDbRef;

        // Check if user has seen the message or not
        ValueEventListener seenListener;
        DatabaseReference userRefForSeen;

        List<ModelChat> chatList;
        AdapterChat adapterChat;


        String hisUid;
        String myUid;
        String hisImage;


        //Volley request queue for notification
        private RequestQueue requestQueue;


        private boolean notify = false;


    private static final  int CAMERA_REQUEST_CODE = 100;
    private static final  int STORAGE_REQUEST_CODE = 200;
    private static final  int IMAGE_PICK_GALLERY_CODE = 300;
    private static final  int IMAGE_PICK_CAMERA_CODE = 400;


    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_uri = null;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);

            // Init views
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle("");

            recyclerView = findViewById(R.id.chat_recyclerView);
            profileIv = findViewById(R.id.profileIv);
            nameTv = findViewById(R.id.nameTv);
            statusTv = findViewById(R.id.statusTv);
            messageEt = findViewById(R.id.messageEt);
            sendBtn = findViewById(R.id.sendBtn);
            attachBtn = findViewById(R.id.attachBtn);

            requestQueue = Volley.newRequestQueue(getApplicationContext());

            //Init arrays of permissions
            cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

            // Layout for recycler view
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);



            Intent intent = getIntent();
            hisUid = intent.getStringExtra("hisUid");

            mAuth = FirebaseAuth.getInstance();

            firebaseDatabase = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/");
            usersDbRef = firebaseDatabase.getReference("Users");

            // Search user to get that user's info
            Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
            // Get user picture and username
            userQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        // Get data
                        String username = "" + ds.child("username").getValue();
                        hisImage = "" + ds.child("image").getValue();
                        String typingStatus = "" + ds.child("typingTo").getValue();

                        // check typing status
                        if(typingStatus.equals(myUid)){
                            statusTv.setText("typing...");
                        }
                        else {
                            // get value of online status
                            String status = "" + ds.child("status").getValue();
                            if(status.equals("online")){
                                statusTv.setText(status);
                            }
                            else {
                                // convert timestamp to proper time date
                                // Convert time stamp to dd/mm/yyyy hh:mm am/pm
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(status));
                                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                                statusTv.setText("Last seen at: "+dateTime);

                            }
                        }


                        // set data
                        nameTv.setText(username);

                        try {
                            Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_image_white).into(profileIv);

                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_default_image_white).into(profileIv);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // Click button to send message
            sendBtn.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    notify = true;
                    // get text from edit text
                    String message = messageEt.getText().toString().trim();
                    // Check if text is empty or not

                    if(TextUtils.isEmpty(message)){
                        //Text empty
                        Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // text not empty
                        sendMessage(message);
                    }

                    messageEt.setText("");
                }
            });

            // Attach button on click
            attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //show image pick dialog
                    showImagePickDialog();
                }
            });

            // check edit text change listener
            messageEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.toString().trim().length() == 0){
                        checkTypingStatus("noOne");
                    }
                    else {
                        checkTypingStatus(hisUid); //uid of receiver
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            readMessages();

            seenMessage();


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
        boolean result = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

        private void seenMessage() {
            userRefForSeen = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Chats");
            seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ModelChat chat = ds.getValue(ModelChat.class);
                        if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen",true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void readMessages() {
            chatList = new ArrayList<>();
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatList.clear();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        ModelChat chat = ds.getValue(ModelChat.class);
                        if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                                chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                            chatList.add(chat);
                        }
                        adapterChat = new AdapterChat(ChatActivity.this,chatList, hisImage);
                        adapterChat.notifyDataSetChanged();
                        recyclerView.setAdapter(adapterChat);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void sendImageMessage(Uri image_uri) throws IOException {
        notify = true;

        // PD
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/"+"post_"+timeStamp;

        // Get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
        byte[] data = baos.toByteArray(); // convert image to bytes
        StorageReference reference = FirebaseStorage.getInstance("gs://indistant-ec7c4.appspot.com/").getReference().child(fileNameAndPath);
        reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded
                progressDialog.dismiss();
                // get url uploaded image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadUri = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //add image uri and other info to database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                    //set up data
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("sender", myUid);
                    hashMap.put("receiver",hisUid);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timestamp",timeStamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen",false);
                    //put this to firebase now
                    databaseReference.child("Chats").push().setValue(hashMap);


                    //Send notification
                    DatabaseReference database = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(myUid);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUsers users = snapshot.getValue(ModelUsers.class);

                            if(notify){
                                sendNotification(hisUid, users.getUsername()," sent you a photo...");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // Chatlist in firebase
                    final DatabaseReference chatRef1 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                            .child(myUid).child(hisUid);

                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef1.child("id").setValue(hisUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    final DatabaseReference chatRef2 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                            .child(hisUid).child(myUid);

                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                progressDialog.dismiss();
            }
        });
    }

        private void sendMessage(String message) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender",myUid);
            hashMap.put("receiver", hisUid);
            hashMap.put("message",message);
            hashMap.put("timestamp",timestamp);
            hashMap.put("isSeen",false);
            hashMap.put("type","text");
            databaseReference.child("Chats").push().setValue(hashMap);

            // Reset edittext after sending message
           // messageEt.setText("");


            String msg = message;
            DatabaseReference database = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(myUid);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUsers user = snapshot.getValue(ModelUsers.class);

                    if(notify){
                        sendNotification(hisUid, user.getUsername(), message);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // Chatlist in firebase
            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                    .child(myUid).child(hisUid);

            chatRef1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        chatRef1.child("id").setValue(hisUid);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                    .child(hisUid).child(myUid);

            chatRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        chatRef2.child("id").setValue(myUid);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void sendNotification(String hisUid, String username, String message) {
            DatabaseReference allTokens = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tokens");

            Query query = allTokens.orderByKey().equalTo(hisUid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Token token = ds.getValue(Token.class);
                        Data data = new Data(""+myUid,
                                ""+username+": "+message,
                                "New Message",
                                ""+hisUid,
                                R.drawable.ic_default_image, "ChatNotification");


                        Sender sender = new Sender(data, token.getToken());
                        //fcm json object request
                        try {
                            JSONObject senderJsonObject = new JSONObject(new Gson().toJson(sender));
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObject,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            // response of the request
                                            Log.d("JSON_RESPONSE", "OnResponse: "+response.toString());
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("JSON_RESPONSE", "OnResponse: "+error.toString());
                                }
                            }){
                                @Nullable
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    // put params
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Authorization","key=AAAAptkeAcs:APA91bEUasvFBsnqiXqtbSogSEReMqfdzyQ-sRCH4ax_A7FnqrmmAiLVy1STEGp8N-yo7O7610m8RFSEMva_2DJSVvI6gu-nhAzVcd_bXCFg9Ui_w9-yE20h0yJUkU240QK52EIY8psb");
                                    return headers;
                                }
                            };
                            //add this request to queue
                            requestQueue.add(jsonObjectRequest);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
                myUid = mUser.getUid();

            } else {
                // If user is singed out
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }

        private void checkStatus(String status){
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(myUid);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status",status);
            // Update value of status of current user
            dbRef.updateChildren(hashMap);
        }

        private void checkTypingStatus(String typing){
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(myUid);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("typingTo",typing);
            // Update value of status of current user
            dbRef.updateChildren(hashMap);
        }

        @Override
        protected void onStart() {
            checkUserStatus();
            //set online
            checkStatus("online");
            super.onStart();
        }

        @Override
        protected void onPause() {
            //get timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());
            // set offline with last seen time
            checkStatus(timestamp);
            checkTypingStatus("noOne");

            super.onPause();
            userRefForSeen.removeEventListener(seenListener);
        }

        @Override
        protected void onResume() {
            checkStatus("online");
            super.onResume();
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
                        Toast.makeText(ChatActivity.this, "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ChatActivity.this , "Please enable storage permissions", Toast.LENGTH_SHORT).show();
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

                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // Image is picked from camera, get uri of image
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            //Hide search view
            menu.findItem(R.id.search).setVisible(false);
            //menu.findItem(R.id.action_add_post).setVisible(false);

            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {

            int id = item.getItemId();
            if (id == R.id.action_logout) {
                mAuth.signOut();
                checkUserStatus();
            }
            if (id == R.id.settings) {
                startActivity(new Intent(ChatActivity.this, SettingsActivity.class));
                return true;
            }


            return super.onOptionsItemSelected(item);
        }
    }