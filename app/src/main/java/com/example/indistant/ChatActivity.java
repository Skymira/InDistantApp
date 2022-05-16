package com.example.indistant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.indistant.adapters.AdapterChat;
import com.example.indistant.models.ModelChat;
import com.example.indistant.models.ModelUsers;
import com.example.indistant.notifications.APIService;
import com.example.indistant.notifications.Client;
import com.example.indistant.notifications.Data;
import com.example.indistant.notifications.Response;
import com.example.indistant.notifications.Sender;
import com.example.indistant.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

        // Views from xml
        Toolbar toolbar;
        RecyclerView recyclerView;
        ImageView profileIv;
        TextView nameTv, statusTv;
        EditText messageEt;
        ImageButton sendBtn;

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

        APIService apiService;
        boolean notify = false;



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

            // Layout for recycler view
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            //create api service
            apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

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

        private void sendMessage(String message) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender",myUid);
            hashMap.put("receiver", hisUid);
            hashMap.put("message",message);
            hashMap.put("timestamp",timestamp);
            hashMap.put("isSeen",false);
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
                        Data data = new Data(myUid, username+":"+message, "New Message", hisUid, R.drawable.ic_default_image);


                        Sender sender = new Sender(data, token.getToken());
                        apiService.sendNotification(sender)
                                .enqueue(new Callback<Response>() {
                                    @Override
                                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                        Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<Response> call, Throwable t) {

                                    }
                                });
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