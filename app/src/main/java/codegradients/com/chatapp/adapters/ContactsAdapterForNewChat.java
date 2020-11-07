package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.ChatActivity;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.R;

public class ContactsAdapterForNewChat extends RecyclerView.Adapter<ContactsAdapterForNewChat.ViewHolder> {

    List<ContactModel> list;
    Context context;

    public ContactsAdapterForNewChat(List<ContactModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsAdapterForNewChat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_card, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapterForNewChat.ViewHolder holder, int position) {

        ContactModel model = list.get(position);

        holder.userName.setText(model.getUserName());
        holder.userAbout.setText(model.getUserAbout());

        try{
            Glide.with(context).load(model.getUserImage()).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(holder.userImage);
        } catch (Exception e){
            e.printStackTrace();
        }

        holder.bgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChatActivity.class).putExtra("userId", model.getUserId()).putExtra("userName", model.getUserName()).putExtra("userImage", model.getUserImage()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView userName, userAbout;
        LinearLayout bgLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.userImageContactCard);
            userName = itemView.findViewById(R.id.userNameContactCard);
            bgLayout = itemView.findViewById(R.id.bgLayoutContactCart);
            userAbout = itemView.findViewById(R.id.userAboutContactCard);
        }
    }
}
