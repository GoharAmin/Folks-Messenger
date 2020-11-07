package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import codegradients.com.chatapp.Models.CommentModel;
import codegradients.com.chatapp.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder> {

    List<CommentModel> list;
    Context context;

    public PostCommentAdapter(List<CommentModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public PostCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.comment_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostCommentAdapter.ViewHolder holder, int position) {
        CommentModel model = list.get(position);

        holder.commenterName.setText(model.getCommenterName());
        holder.comment.setText(model.getComment());

        Glide.with(context).load(model.getCommenterImage()).into(holder.commenterImage);

        try {
            double currentt = System.currentTimeMillis() - Long.parseLong(model.getTime());

            double ttime = currentt / 1000;
            double minuttes  = ttime / 60;

            int ttimestamp = (int) minuttes;

            if (ttimestamp > 60){

                int hours = ttimestamp / 60;

                int minutess = ttimestamp % 60;

                if (hours > 24){

                    long curre = Long.parseLong(model.getTime());

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String dateString = formatter.format(new Date(curre));
                    holder.commentTime.setText(dateString);

                } else {
                    holder.commentTime.setText(String.valueOf(hours) + " h " + String.valueOf(minutess) + " min ago");
                }

            }else {

                if (ttimestamp == 0){
                    holder.commentTime.setText("Just Now");
                } else {
                    holder.commentTime.setText(String.valueOf(ttimestamp) + " minutes ago");
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

        CircleImageView commenterImage;
        TextView commenterName, commentTime, comment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            comment = itemView.findViewById(R.id.commentCommentCard);
            commenterImage = itemView.findViewById(R.id.commenterImageCommentCard);
            commenterName = itemView.findViewById(R.id.commenterNameCommentCard);
            commentTime= itemView.findViewById(R.id.commentTimeCommentCard);
        }
    }
}
