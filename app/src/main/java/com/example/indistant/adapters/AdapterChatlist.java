package com.example.indistant.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.indistant.ChatActivity;
import com.example.indistant.R;
import com.example.indistant.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUsers> usersList; //Get user info
    private HashMap<String, String> lastMessageMap;


    public AdapterChatlist(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
       lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get data
        String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getImage();
        String userUsername = usersList.get(position).getUsername();
        String userCountry = usersList.get(position).getCountry();
        String lastMessage = lastMessageMap.get(hisUid);

        // Set data

        holder.usernameTextTv.setText(userUsername);
        holder.countryTextTv.setText(userCountry);

        if(lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.profileImageIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_image).into(holder.profileImageIv);
        }

        // Set status
        if(usersList.get(position).getStatus().equals("online")){
           holder.statusIv.setImageResource(R.drawable.circle_online);

        }
        else{
            holder.statusIv.setImageResource(R.drawable.circle_offline);
        }

        // User chatlist click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start chat activity
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        // Views of row_chatlist.xml
        ImageView profileImageIv, statusIv;
        TextView usernameTextTv, countryTextTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            profileImageIv = itemView.findViewById(R.id.profileImageIv);
            statusIv = itemView.findViewById(R.id.statusIv);
            usernameTextTv = itemView.findViewById(R.id.usernameTextTv);
            countryTextTv = itemView.findViewById(R.id.countryTextTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
