package com.example.indistant.adapters;

import android.content.Context;
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
import com.example.indistant.models.ModelUsers;
import com.example.indistant.ChatActivity;
import com.example.indistant.R;
import com.example.indistant.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUsers>userList;

    public AdapterUsers(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
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
        String userCountries = userList.get(position).getCountries();

        // Set data
        holder.mUsernameTv.setText(userName);
        holder.mCountriesTv.setText(userCountries);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.mAvatarIv);
        }
        catch (Exception e){

        }
        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(context, ""+userCountries, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUID);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // View holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mUsernameTv, mCountriesTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mUsernameTv = itemView.findViewById(R.id.nameTv);
            mCountriesTv = itemView.findViewById(R.id.countries_userTv);

        }
    }



}
