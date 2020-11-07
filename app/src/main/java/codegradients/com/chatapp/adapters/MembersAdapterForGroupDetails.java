package codegradients.com.chatapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.GroupDetailActivity;
import codegradients.com.chatapp.Models.MemberModel;
import codegradients.com.chatapp.R;

public class MembersAdapterForGroupDetails extends RecyclerView.Adapter<MembersAdapterForGroupDetails.ViewHolder> {

    List<MemberModel> list;
    Context context;

    String groupId;

    Boolean isAdmin;

    public MembersAdapterForGroupDetails(List<MemberModel> list, Context context, String groupId, Boolean isAdmin) {
        this.list = list;
        this.context = context;
        this.groupId = groupId;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public MembersAdapterForGroupDetails.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.member_card_group_detail, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MembersAdapterForGroupDetails.ViewHolder holder, int position) {

        final MemberModel model = list.get(position);

        holder.userName.setText(model.getName());

        if (model.getStatus().equals("user")){
            holder.userStatus.setText("User");
        } else if (model.getStatus().equals("admin")){
            holder.userStatus.setText("Admin");
        }

        //holder.userStatus.setText(model.getStatus());

        Glide.with(context).load(model.getImage()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                holder.avi.setVisibility(View.GONE);
                holder.memberImage.setImageResource(R.drawable.ic_camera);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                holder.avi.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.memberImage);

        if (isAdmin){
            holder.moreImg.setVisibility(View.VISIBLE);
        } else {
            holder.moreImg.setVisibility(View.GONE);
        }

        holder.moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] colors = {"", "Remove From Group"};
                if (model.getStatus().equals("user")){
                    colors[0] = "Make Admin";
                } else {
                    colors[0] = "Make User";
                }

                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            if (which == 0) {
                                DatabaseReference mDatabaseForChaningStatus = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users").child(model.getId());
                                if (model.getStatus().equals("user")){
                                    mDatabaseForChaningStatus.setValue("admin");
                                    GroupDetailActivity.getGroupDetailActivity().getMembers();
                                } else {
                                    mDatabaseForChaningStatus.setValue("user");
                                    GroupDetailActivity.getGroupDetailActivity().getMembers();
                                }

                            } else {
                                DatabaseReference mDatabaseForRemoving = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users").child(model.getId());
                                mDatabaseForRemoving.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                                        GroupDetailActivity.getGroupDetailActivity().getMembers();
                                    }
                                });
                            }
                        }
                    });
                    builder.show();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        if (isAdmin){
            holder.parentRl.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    String[] colors = {"Gallery", "Remove From Group"};
                    if (model.getStatus().equals("user")){
                        colors[0] = "Make Admin";
                    } else {
                        colors[0] = "Make User";
                    }

                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("");
                        builder.setItems(colors, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // the user clicked on colors[which]
                                if (which == 0) {
                                    DatabaseReference mDatabaseForChaningStatus = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users").child(model.getId());
                                    if (model.getStatus().equals("user")){
                                        mDatabaseForChaningStatus.setValue("admin");
                                        GroupDetailActivity.getGroupDetailActivity().getMembers();
                                    } else {
                                        mDatabaseForChaningStatus.setValue("user");
                                        GroupDetailActivity.getGroupDetailActivity().getMembers();
                                    }

                                } else {
                                    DatabaseReference mDatabaseForRemoving = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users").child(model.getId());
                                    mDatabaseForRemoving.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                                            GroupDetailActivity.getGroupDetailActivity().getMembers();
                                        }
                                    });
                                }
                            }
                        });
                        builder.show();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView memberImage;
        RelativeLayout parentRl;
        AVLoadingIndicatorView avi;
        ImageView moreImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_Name_member_card);
            userStatus = itemView.findViewById(R.id.member_status_card);
            parentRl = itemView.findViewById(R.id.member_card_parent);
            memberImage = itemView.findViewById(R.id.member_Image_card);
            avi = itemView.findViewById(R.id.avi_member_card);
            moreImg = itemView.findViewById(R.id.more_btn_member_card_grp_detail);
        }
    }
}
