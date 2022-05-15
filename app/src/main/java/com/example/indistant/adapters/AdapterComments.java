package com.example.indistant.adapters;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.indistant.R;
import com.example.indistant.models.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{

    Context context;
    List<ModelComment> commentsList;

    public AdapterComments(Context context, List<ModelComment> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get the data
        String uid = commentsList.get(position).getUid();
        String username = commentsList.get(position).getuUsername();
        String email = commentsList.get(position).getuEmail();
        String image = commentsList.get(position).getuDp();
        String comment = commentsList.get(position).getComment();
        String country = commentsList.get(position).getuCountry();
        String timestamp = commentsList.get(position).getTimestamp();
        String cid = commentsList.get(position).getcId();

        // convert time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set the data
        holder.usernameTv.setText(username);
        holder.countryTv.setText(country);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        //Set user dp

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_image).into(holder.avatarIv);
        }
        catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //declare views from row_comments.xml
        ImageView avatarIv;
        TextView usernameTv, countryTv, commentTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            countryTv = itemView.findViewById(R.id.countryTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
