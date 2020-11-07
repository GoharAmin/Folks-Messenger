package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Calendar;
import java.util.List;

import codegradients.com.chatapp.Activities.UserProfileActivity;
import codegradients.com.chatapp.Models.CallRecordModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;
import de.hdodenhof.circleimageview.CircleImageView;

public class CallRecordAdapter extends RecyclerView.Adapter<CallRecordAdapter.ViewHolder> {

    List<CallRecordModel> list;
    Context context;

    public CallRecordAdapter(List<CallRecordModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public CallRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.call_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CallRecordAdapter.ViewHolder holder, int position) {

        CallRecordModel model = list.get(position);

        holder.imageProgressAvi.setVisibility(View.GONE);

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherPersonId = "";
        if (myId.equals(model.getCalleeId())){
            otherPersonId = model.getCallerId();
        } else {
            otherPersonId = model.getCalleeId();
        }

        DatabaseReference mDatabaseForGettingInformation = FirebaseDatabase.getInstance().getReference().child("users").child(otherPersonId);
        mDatabaseForGettingInformation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String imageLink = HelperClass.decrypt(dataSnapshot.child("profileImage").getValue(String.class));
                    Glide.with(context).load(imageLink).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.imageProgressAvi.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(holder.personImage);

                    String nameOfPerson = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                    holder.personName.setText(nameOfPerson);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        long timeStamp = Long.parseLong(model.getPlacingTime());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String format = "";
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        String minutee;

        if (minute < 10) {
            minutee = String.valueOf(0) + String.valueOf(minute);
        } else {
            minutee = String.valueOf(minute);
        }

        String messageTime = String.valueOf(hour) + ":" + minutee + " " + format;

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
            messageTime = messageTime + ", Today";
            //messageTime = messageTime;
            holder.callTime.setText(messageTime);
        } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
            messageTime = messageTime + ", Yesterday";
            holder.callTime.setText(messageTime);
        } else {

            int mon = calendar.get(Calendar.MONTH);
            mon ++;

            messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
            holder.callTime.setText(messageTime);
        }

        if (model.isVideo()){
            holder.callTypeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_videocam_white_24dp));
        } else {
            holder.callTypeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_call_white_24dp));
        }

        holder.bgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String idToSend = "";
                if (myId.equals(model.getCalleeId())){
                    idToSend = model.getCallerId();
                } else {
                    idToSend = model.getCalleeId();
                }

               context.startActivity(new Intent(context, UserProfileActivity.class).putExtra("userId", idToSend));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView personImage;
        AVLoadingIndicatorView imageProgressAvi;
        TextView personName, callTime;
        ImageView callTypeImage;

        RelativeLayout bgLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            callTypeImage = itemView.findViewById(R.id.callTypeImage);
            personImage = itemView.findViewById(R.id.user_Image_card);
            imageProgressAvi = itemView.findViewById(R.id.user_Image_card_avi);
            personName = itemView.findViewById(R.id.user_Name_call_card);
            callTime = itemView.findViewById(R.id.call_time_call_card);
            bgLayout = itemView.findViewById(R.id.parent_user_inbox_card);
        }
    }
}
