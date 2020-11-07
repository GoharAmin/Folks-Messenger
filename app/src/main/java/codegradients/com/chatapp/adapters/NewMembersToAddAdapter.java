package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.AddNewMembersToGroupActivity;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.R;

public class NewMembersToAddAdapter extends RecyclerView.Adapter<NewMembersToAddAdapter.ViewHolder> {

    List<ContactModel> list;
    Context context;

    public NewMembersToAddAdapter(List<ContactModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public NewMembersToAddAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_new_card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewMembersToAddAdapter.ViewHolder holder, int position) {

        final ContactModel model = list.get(position);

        holder.userName.setText(model.getUserName());

        Glide.with(context).load(model.getUserImage()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                holder.userImage.setImageResource(R.drawable.ic_person_add_white_24dp);
                holder.avi.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                holder.avi.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.userImage);

        holder.parentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AddNewMembersToGroupActivity.newMembers.contains(model)){
                    AddNewMembersToGroupActivity.newMembers.remove(model);
                    holder.parentRl.setBackgroundColor(context.getResources().getColor(R.color.white));
                } else {
                    AddNewMembersToGroupActivity.newMembers.add(model);
                    holder.parentRl.setBackgroundColor(Color.parseColor("#553B61F6"));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        RelativeLayout parentRl;
        AVLoadingIndicatorView avi;
        CircleImageView userImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_Name_new_user_card);
            parentRl = itemView.findViewById(R.id.user_card_new_card_parent);
            avi = itemView.findViewById(R.id.avi_new_member_card);
            userImage = itemView.findViewById(R.id.new_user_Image_card);
        }
    }
}
