package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.Models.PostsModel;
import codegradients.com.chatapp.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    List<PostsModel> list;
    Context context;

    public PostsAdapter(List<PostsModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsAdapter.ViewHolder holder, int position) {

        final PostsModel model = list.get(position);

        int dataSize = 0;

        try {
            dataSize = model.getData().size();
        } catch (Exception e){
            e.printStackTrace();
        }

        holder.postDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getMainActivity().goToPostDetail(model);
            }
        });

        holder.commentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getMainActivity().goToPostDetail(model);
            }
        });

        if (model.getLikedBy().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.liked_iccc));
        } else {
            holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.like_iccc));
        }

        holder.likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference mDatabaseForLiking = FirebaseDatabase.getInstance().getReference().child("StatusPosts").child(model.getPostId());
                if (model.getLikedBy().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    mDatabaseForLiking.child("likedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                    holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.like_iccc));
                } else {
                    mDatabaseForLiking.child("likedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                    holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.liked_iccc));
                }
            }
        });

        if (model.getLikedBy().size() == 0){
            holder.likeNumberText.setVisibility(View.INVISIBLE);
        } else {
            holder.likeNumberText.setVisibility(View.VISIBLE);
            holder.likeNumberText.setText(String.valueOf(model.getLikedBy().size()));
        }

        if (model.getCommentsCount() == 0){
            holder.commentNumberText.setVisibility(View.INVISIBLE);
        } else {
            holder.commentNumberText.setVisibility(View.VISIBLE);
            holder.commentNumberText.setText(String.valueOf(model.getCommentsCount()));
        }

        holder.postDescription.setText(model.getDescription());
        holder.postTitle.setText(model.getPosterName());

        try {
            Glide.with(context).load(model.getPosterImage()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.postImage.setImageResource(R.drawable.ic_smartphone);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(holder.postImage);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (dataSize == 0){
            //holder.postImage.setImageResource(R.drawable.ic_smartphone);
            holder.dataLayout.setVisibility(View.GONE);
        } else {
            holder.dataLayout.setVisibility(View.VISIBLE);

            holder.dataLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.getMainActivity().goToPostDetail(model);
                }
            });

            if (dataSize == 1){
                holder.oneImageLayout.setVisibility(View.VISIBLE);
                holder.twoImageLayout.setVisibility(View.GONE);
                holder.threeImageLayout.setVisibility(View.GONE);
                holder.fourImageLayout.setVisibility(View.GONE);

                if (model.getData().get(0).getType().equals("video")){
                    holder.oneImageLayoutImage1Play.setVisibility(View.VISIBLE);
                } else {
                    holder.oneImageLayoutImage1Play.setVisibility(View.GONE);
                }

                try {
                    Glide.with(context).load(model.getData().get(0).getLink()).into(holder.oneImageLayoutImage1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (dataSize == 2){
                holder.oneImageLayout.setVisibility(View.GONE);
                holder.twoImageLayout.setVisibility(View.VISIBLE);
                holder.threeImageLayout.setVisibility(View.GONE);
                holder.fourImageLayout.setVisibility(View.GONE);

                if (model.getData().get(0).getType().equals("video")){
                    holder.twoImageLayoutImage1Play.setVisibility(View.VISIBLE);
                } else {
                    holder.twoImageLayoutImage1Play.setVisibility(View.GONE);
                }

                if (model.getData().get(1).getType().equals("video")){
                    holder.twoImageLayoutImage2Play.setVisibility(View.VISIBLE);
                } else {
                    holder.twoImageLayoutImage2Play.setVisibility(View.GONE);
                }

                try {
                    Glide.with(context).load(model.getData().get(0).getLink()).into(holder.twoImageLayoutImage1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(1).getLink()).into(holder.twoImageLayoutImage2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (dataSize == 3){
                holder.oneImageLayout.setVisibility(View.GONE);
                holder.twoImageLayout.setVisibility(View.GONE);
                holder.threeImageLayout.setVisibility(View.VISIBLE);
                holder.fourImageLayout.setVisibility(View.GONE);

                if (model.getData().get(0).getType().equals("video")){
                    holder.threeImageLayoutImage1Play.setVisibility(View.VISIBLE);
                } else {
                    holder.threeImageLayoutImage1Play.setVisibility(View.GONE);
                }

                if (model.getData().get(1).getType().equals("video")){
                    holder.threeImageLayoutImage2Play.setVisibility(View.VISIBLE);
                } else {
                    holder.threeImageLayoutImage2Play.setVisibility(View.GONE);
                }

                if (model.getData().get(2).getType().equals("video")){
                    holder.threeImageLayoutImage3Play.setVisibility(View.VISIBLE);
                } else {
                    holder.threeImageLayoutImage3Play.setVisibility(View.GONE);
                }

                try {
                    Glide.with(context).load(model.getData().get(0).getLink()).into(holder.threeImageLayoutImage1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(1).getLink()).into(holder.threeImageLayoutImage2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(2).getLink()).into(holder.threeImageLayoutImage3);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                holder.oneImageLayout.setVisibility(View.GONE);
                holder.twoImageLayout.setVisibility(View.GONE);
                holder.threeImageLayout.setVisibility(View.GONE);
                holder.fourImageLayout.setVisibility(View.VISIBLE);

                if (dataSize > 4){
                    holder.moreThanFourLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.moreThanFourLayout.setVisibility(View.GONE);
                }

                if (model.getData().get(0).getType().equals("video")){
                    holder.fourImagelayoutImage1Play.setVisibility(View.VISIBLE);
                } else {
                    holder.fourImagelayoutImage1Play.setVisibility(View.GONE);
                }

                if (model.getData().get(1).getType().equals("video")){
                    holder.fourImageLayoutImage2Play.setVisibility(View.VISIBLE);
                } else {
                    holder.fourImageLayoutImage2Play.setVisibility(View.GONE);
                }

                if (model.getData().get(2).getType().equals("video")){
                    holder.fourImageLayoutImage3Play.setVisibility(View.VISIBLE);
                } else {
                    holder.fourImageLayoutImage3Play.setVisibility(View.GONE);
                }

                if (model.getData().get(3).getType().equals("video")){
                    holder.fourImageLayoutImage4Play.setVisibility(View.VISIBLE);
                } else {
                    holder.fourImageLayoutImage4Play.setVisibility(View.GONE);
                }

                try {
                    Glide.with(context).load(model.getData().get(0).getLink()).into(holder.fourImageLayoutImage1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(1).getLink()).into(holder.fourImageLayoutImage2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(2).getLink()).into(holder.fourImageLayoutImage3);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Glide.with(context).load(model.getData().get(3).getLink()).into(holder.fourImageLayoutImage4);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            double currentt = System.currentTimeMillis() - model.getCreated_at();

            double ttime = currentt / 1000;
            double minuttes  = ttime / 60;

            int ttimestamp = (int) minuttes;

            if (ttimestamp > 60){

                int hours = ttimestamp / 60;

                int minutess = ttimestamp % 60;

                if (hours > 24){

                    long curre = model.getCreated_at();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String dateString = formatter.format(new Date(curre));
                    holder.postTime.setText(dateString);

                } else {
                    holder.postTime.setText(String.valueOf(hours) + " h " + String.valueOf(minutess) + " min ago");
                }

            }else {

                if (ttimestamp == 0){
                    holder.postTime.setText("Just Now");
                } else {
                    holder.postTime.setText(String.valueOf(ttimestamp) + " minutes ago");
                }
            }
        } catch (Exception e){
            Log.v("Errorrr: " , e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView postTitle, postTime, postDescription;
        CircleImageView postImage;

        TextView likeNumberText, commentNumberText;
        ImageView likeImage, commentImage;

        RelativeLayout dataLayout, threeImageLayout, moreThanFourLayout;
        LinearLayout oneImageLayout, twoImageLayout, fourImageLayout;

        ImageView oneImageLayoutImage1, twoImageLayoutImage1, twoImageLayoutImage2, threeImageLayoutImage1, threeImageLayoutImage2, threeImageLayoutImage3, fourImageLayoutImage1, fourImageLayoutImage2, fourImageLayoutImage3, fourImageLayoutImage4;
        ImageView oneImageLayoutImage1Play, twoImageLayoutImage1Play, twoImageLayoutImage2Play, threeImageLayoutImage1Play, threeImageLayoutImage2Play, threeImageLayoutImage3Play, fourImagelayoutImage1Play, fourImageLayoutImage2Play, fourImageLayoutImage3Play, fourImageLayoutImage4Play;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postTitle = itemView.findViewById(R.id.postTitlePostCard);
            postTime = itemView.findViewById(R.id.postTimePostCard);
            postImage = itemView.findViewById(R.id.postImagePostCard);
            postDescription = itemView.findViewById(R.id.postDescriptionPostCard);

            likeImage = itemView.findViewById(R.id.likeIconPostCard);
            likeNumberText = itemView.findViewById(R.id.likeNumberPostCard);
            commentImage = itemView.findViewById(R.id.commentIconPostCard);
            commentNumberText = itemView.findViewById(R.id.commentNumberPostCard);

            dataLayout = itemView.findViewById(R.id.dataLayout);
            moreThanFourLayout = itemView.findViewById(R.id.moreThanFourLayout);

            oneImageLayout = itemView.findViewById(R.id.oneImageLayout);
            twoImageLayout = itemView.findViewById(R.id.twoImageLayout);
            threeImageLayout = itemView.findViewById(R.id.threeImageLayout);
            fourImageLayout = itemView.findViewById(R.id.fourImageLayout);

            oneImageLayoutImage1 = itemView.findViewById(R.id.oneImageLayoutImage);
            oneImageLayoutImage1Play = itemView.findViewById(R.id.oneImageLayoutImagePlay);

            twoImageLayoutImage1 = itemView.findViewById(R.id.twoImageLayoutImage1);
            twoImageLayoutImage1Play = itemView.findViewById(R.id.twoImageLayoutImage1Play);
            twoImageLayoutImage2 = itemView.findViewById(R.id.twoImageLayoutImage2);
            twoImageLayoutImage2Play = itemView.findViewById(R.id.twoImageLayoutImage2Play);

            threeImageLayoutImage1 = itemView.findViewById(R.id.threeImageLayoutImage1);
            threeImageLayoutImage1Play = itemView.findViewById(R.id.threeImageLayoutImage1Play);
            threeImageLayoutImage2 = itemView.findViewById(R.id.threeImageLayoutImage2);
            threeImageLayoutImage2Play = itemView.findViewById(R.id.threeImageLayoutImage2Play);
            threeImageLayoutImage3 = itemView.findViewById(R.id.threeImageLayoutImage3);
            threeImageLayoutImage3Play = itemView.findViewById(R.id.threeImageLayoutImage3Play);

            fourImageLayoutImage1 = itemView.findViewById(R.id.fourImageLayoutImage1);
            fourImagelayoutImage1Play = itemView.findViewById(R.id.fourImageLayoutImage1Play);
            fourImageLayoutImage2 = itemView.findViewById(R.id.fourImageLayoutImage2);
            fourImageLayoutImage2Play = itemView.findViewById(R.id.fourImageLayoutImage2Play);
            fourImageLayoutImage3 = itemView.findViewById(R.id.fourImageLayoutImage3);
            fourImageLayoutImage3Play = itemView.findViewById(R.id.fourImageLayoutImage3Play);
            fourImageLayoutImage4 = itemView.findViewById(R.id.fourImageLayoutImage4);
            fourImageLayoutImage4Play = itemView.findViewById(R.id.fourImageLayoutImage4Play);
        }
    }
}
