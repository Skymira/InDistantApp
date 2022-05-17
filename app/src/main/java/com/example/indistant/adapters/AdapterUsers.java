package com.example.indistant.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.indistant.R;
import com.example.indistant.ThereProfileActivity;
import com.example.indistant.models.ModelUsers;
import com.example.indistant.ChatActivity;
import com.example.indistant.R;
import com.example.indistant.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUsers>userList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUsers(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout (row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get data
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getUsername();
        String userCountries = userList.get(position).getCountry();

        // Set data
        holder.mUsernameTv.setText(userName);
        holder.mCountriesTv.setText(userCountries);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.mAvatarIv);
        }
        catch (Exception e){

        }

        holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        checkIsBlocked(hisUID, holder, position);

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            // profile clicked
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);
                        }
                        if(i == 1){
                            // Chat clicked
                            // Toast.makeText(context, ""+userCountries, Toast.LENGTH_SHORT).show();
                            inBlockedORNot(hisUID);

                        }
                    }
                });
                builder.create().show();
            }
        });
        // Click to unblock user
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userList.get(position).isBlocked()) {
                    unBlockUser(hisUID);
                }
                else {
                    blockUser(hisUID);
                }
            }
        });
    }

    private void inBlockedORNot(String hisUID) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()) {
                                Toast.makeText(context, "You are blocked by that user!", Toast.LENGTH_SHORT).show();
                                // blocked dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, MyHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()) {
                                holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                                userList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        // Block the user, by adding uid to current user's "BlockedUsers" node

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // blocked successfully
                        Toast.makeText(context, "User Blocked!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed to block
                Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUID) {
        // unblock by removing it
        DatabaseReference ref = FirebaseDatabase.getInstance("https://indistant-ec7c4-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.exists()) {
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //unblocked successfully
                                                Toast.makeText(context, "User Unblocked!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to unblock
                                        Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // View holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv, blockIv;
        TextView mUsernameTv, mCountriesTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            blockIv = itemView.findViewById(R.id.blockIv);
            mUsernameTv = itemView.findViewById(R.id.nameTv);
            mCountriesTv = itemView.findViewById(R.id.countries_userTv);

        }
    }



}
