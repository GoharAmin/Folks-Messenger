package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.CreateNewGroupActivity;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.R;

public class NewGroupMakingAdapter extends RecyclerView.Adapter<NewGroupMakingAdapter.ViewHolder> {

    List<ContactModel> list;
    Context context;

    public NewGroupMakingAdapter(List<ContactModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public NewGroupMakingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.create_group_member_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewGroupMakingAdapter.ViewHolder holder, int position) {

        final ContactModel model = list.get(position);

        holder.userName.setText(model.getUserName());

        Glide.with(context).load(model.getUserImage()).into(holder.userImg);

        holder.parentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CreateNewGroupActivity.NewMembersForGroup.contains(model)){
                    CreateNewGroupActivity.NewMembersForGroup.remove(model);
                    holder.parentRl.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    CreateNewGroupActivity.NewMembersForGroup.add(model);
                    holder.parentRl.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImg;
        TextView userName;
        RelativeLayout parentRl;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImg = itemView.findViewById(R.id.user_img_crt_grp_card);
            userName = itemView.findViewById(R.id.user_name_crt_grp_card);
            parentRl = itemView.findViewById(R.id.parent_rl_crt_grp_card);
        }
    }
}
