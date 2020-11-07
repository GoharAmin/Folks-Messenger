package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import codegradients.com.chatapp.Activities.ForwardMessagesActivity;
import codegradients.com.chatapp.Models.ContactsForForwardScreenModel;
import codegradients.com.chatapp.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardMessageScreenAdapter extends RecyclerView.Adapter<ForwardMessageScreenAdapter.ViewHolder> {

    List<ContactsForForwardScreenModel> list;
    Context context;

    public ForwardMessageScreenAdapter(List<ContactsForForwardScreenModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ForwardMessageScreenAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_new_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ForwardMessageScreenAdapter.ViewHolder holder, int position) {

        ContactsForForwardScreenModel model = list.get(position);

        holder.nameView.setText(model.getSelectedName());

        Glide.with(context).
                load(model.getSelectedImage()).
                apply(new RequestOptions().placeholder(R.drawable.progress_animation).error(R.drawable.ic_baseline_error_outline_24)).
                into(holder.imageView);

        try {
            if (containsCheck(model)) {
                holder.parentBg.setBackgroundColor(Color.parseColor("#333B61F6"));
            } else {
                holder.parentBg.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        } catch (Exception e){}


        holder.parentBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (containsCheck(model)){
                   // Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                    removeModelFromSelectedList(model);
                    holder.parentBg.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                    addModelToSelectedList(model);
                    holder.parentBg.setBackgroundColor(Color.parseColor("#333B61F6"));
                }
            }
        });
    }

    private void addModelToSelectedList(ContactsForForwardScreenModel mm){
        ForwardMessagesActivity.listForSelectedModels.add(mm);

    }

    private void removeModelFromSelectedList(ContactsForForwardScreenModel mm) {
        int position = 1110;

        for (int i = 0; i < ForwardMessagesActivity.listForSelectedModels.size(); i++) {
            ContactsForForwardScreenModel modelForward = ForwardMessagesActivity.listForSelectedModels.get(i);
            if (modelForward.getSelectedId().equals(mm.getSelectedId())) {
                position = i;
            }
        }

        if (position != 1110) {
            ForwardMessagesActivity.listForSelectedModels.remove(position);
        }
    }

    private boolean containsCheck(ContactsForForwardScreenModel mm) {

        boolean contains = false;
        for (ContactsForForwardScreenModel modelChat: ForwardMessagesActivity.listForSelectedModels) {
            if (mm.getSelectedId().equals(modelChat.getSelectedId())) {
                contains = true;
            }
        }

        return contains;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView nameView;
        AVLoadingIndicatorView avi;
        RelativeLayout parentBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentBg = itemView.findViewById(R.id.user_card_new_card_parent);
            imageView = itemView.findViewById(R.id.new_user_Image_card);
            nameView = itemView.findViewById(R.id.user_Name_new_user_card);
            avi = itemView.findViewById(R.id.avi_new_member_card);
            avi.setVisibility(View.GONE);
        }
    }
}
