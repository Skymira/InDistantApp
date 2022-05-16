package com.example.indistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.indistant.adapters.AdapterChatlist;
import com.example.indistant.models.ModelChat;
import com.example.indistant.models.ModelChatlist;
import com.example.indistant.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

        FirebaseAuth firebaseAuth;
        RecyclerView recyclerView;
        List<ModelChatlist> chatlistList;
        List<ModelUsers> usersList;
        DatabaseReference reference;
        FirebaseUser currentUser;
        AdapterChatlist adapterChatlist;

        public ChatListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view =  inflater.inflate(R.layout.fragment_chat_list, container, false);

            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            recyclerView = view.findViewById(R.id.recyclerView);
            chatlistList = new ArrayList<>();

            reference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist").child(currentUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatlistList.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                        chatlistList.add(chatlist);
                    }
                    loadChats();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



            return view;
        }

    private void loadChats() {
            usersList = new ArrayList<>();
            reference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usersList.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        ModelUsers user = ds.getValue(ModelUsers.class);
                        for(ModelChatlist chatlist : chatlistList){
                            if(user.getUid() != null && user.getUid().equals(chatlist.getId())){
                                usersList.add(user);
                                break;
                            }
                        }
                        //adapter
                        adapterChatlist = new AdapterChatlist(getContext(), usersList);
                        //set
                        recyclerView.setAdapter(adapterChatlist);
                        //set last message

                        for(int i = 0; i < usersList.size(); i++){
                            lastMessage(usersList.get(i).getUid());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void lastMessage(String userId) {
       DatabaseReference reference = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender == null || receiver == null) {
                        continue;
                    }
                    if(chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){
                        theLastMessage = chat.getMessage();
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // To show menu option in fragment
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
}

