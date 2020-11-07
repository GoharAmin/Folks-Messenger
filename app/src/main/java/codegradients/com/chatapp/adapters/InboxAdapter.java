package codegradients.com.chatapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.ChatActivity;
import codegradients.com.chatapp.Activities.GroupChatActivity;
import codegradients.com.chatapp.Models.InboxModel;
import codegradients.com.chatapp.R;

public class InboxAdapter extends RecyclerView.Adapter {

    private List<InboxModel> list;
    private Context context;
    private static String TAG = InboxAdapter.class.getName();

    public static class PersonelTypeViewHolder extends RecyclerView.ViewHolder {

        TextView nameUser, message, time;
        CircleImageView imageUser;

        RelativeLayout parentBg;


        public PersonelTypeViewHolder(View itemView) {
            super(itemView);

            this.nameUser = (TextView) itemView.findViewById(R.id.user_Name_card);
            this.imageUser = itemView.findViewById(R.id.user_Image_card);
            this.message = itemView.findViewById(R.id.last_Message_card);
            this.time = itemView.findViewById(R.id.last_Message_Time_card);
            this.parentBg = itemView.findViewById(R.id.parent_user_inbox_card);
        }
    }

    public InboxAdapter(List<InboxModel> list, Context context) {
        this.list = list;
        this.context = context;

       // Log.v("HereCalled__", "Size In Adapter: " + list.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case InboxModel.PERSONEL_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.inbox_user_card, parent, false);
                return new PersonelTypeViewHolder(view);

            case InboxModel.GROUP_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.inbox_group_card, parent, false);
                return new GroupTypeViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {

        switch (list.get(position).type) {
            case 0:
                return InboxModel.PERSONEL_TYPE;
            case 1:
                return InboxModel.GROUP_TYPE;

            default:
                return -1;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class GroupTypeViewHolder extends RecyclerView.ViewHolder {

        TextView groupName, lastMessageSender, lastMessageTime, lastMessage;
        CircleImageView groupImage;

        RelativeLayout parentBg;


        public GroupTypeViewHolder(@NonNull View itemView) {
            super(itemView);

            this.groupName = itemView.findViewById(R.id.group_Name_card);
            this.groupImage = itemView.findViewById(R.id.group_Image_card);
            this.lastMessageSender = itemView.findViewById(R.id.last_Message_Sender_card);
            this.lastMessage = itemView.findViewById(R.id.last_Message_card);
            this.lastMessageTime = itemView.findViewById(R.id.last_Message_Time_card);
            this.parentBg = itemView.findViewById(R.id.parent_bg_group_inbox);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final InboxModel model = list.get(position);

        Log.v("HereCalled__", "Here in Adapter at start: " + model.type);

        if (model != null) {

            switch (model.type) {
                case InboxModel.PERSONEL_TYPE:
                    try {
                        ((PersonelTypeViewHolder) holder).parentBg.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                new AlertDialog.Builder(context)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Delete")
                                        .setMessage("Do You really want to delete this chat?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                DatabaseReference mDatabaseForDeletingCurrentChat = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(model.otherId);
                                                mDatabaseForDeletingCurrentChat.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        MDToast.makeText(context, "Chat Deleted", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                                    }
                                                });

                                            }

                                        })
                                        .setNegativeButton("No", null)
                                        .show();

                                return false;
                            }
                        });

                        ((PersonelTypeViewHolder) holder).nameUser.setText(model.name);

                        //final ContactModel user = new ContactModel(model.otherId, model.name, model., model.nickName);

                        Glide.with(context).
                                load(model.img).
                                apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person)).
                                into(((PersonelTypeViewHolder) holder).imageUser);

                        ((PersonelTypeViewHolder) holder).parentBg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("userId", model.otherId);
                                intent.putExtra("userName", model.name);
                                intent.putExtra("userImage", model.img);
                                context.startActivity(intent);
                            }
                        });

                        long messageLongs = Long.parseLong(model.lastMessageTimeStamp);

                        Calendar nowCalender = Calendar.getInstance();
                        Calendar messageTimeCalender = Calendar.getInstance();
                        messageTimeCalender.setTimeInMillis(messageLongs);

                        if (DateUtils.isToday(messageLongs)) {
                            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
                            String dateString = formatter.format(new Date(Long.parseLong(model.lastMessageTimeStamp)));
                            ((PersonelTypeViewHolder) holder).time.setText(dateString);
                        } else if (nowCalender.get(Calendar.DATE) == (messageTimeCalender.get(Calendar.DATE) + 1)) {
                            ((PersonelTypeViewHolder) holder).time.setText("Yesterday");
                        } else {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            String dateString = formatter.format(new Date(messageLongs));
                            ((PersonelTypeViewHolder) holder).time.setText(dateString);
                        }

//                        double current = System.currentTimeMillis() - Double.parseDouble(model.lastMessageTimeStamp);
//
//                        double time = current / 1000;
//                        double minutes  = time / 60;
//
//                        int timestamp = (int) minutes;
//
//                        if (timestamp > 60){
//                            int hours = timestamp / 60;
//
//                            int minutess = timestamp % 60;
//
//                            if (hours > 48){
//                                long curre = Long.parseLong(model.lastMessageTimeStamp);
//
//                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//                                String dateString = formatter.format(new Date(curre));
//                                ((PersonelTypeViewHolder) holder).time.setText(dateString);
//
//                            } else if (hours < 48 && hours > 24) {
//
//                                ((PersonelTypeViewHolder) holder).time.setText("Yesterday");
//
//                            } else if (hours < 24){
//                                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
//                                String dateString = formatter.format(new Date(Long.parseLong(model.lastMessageTimeStamp)));
//                                ((PersonelTypeViewHolder) holder).time.setText(dateString);
//
//                                //((PersonelTypeViewHolder) holder).time.setText(String.valueOf(hours) + " h " + String.valueOf(minutess) + " min ago");
//                            }
//
//                        }else {
//                            if (timestamp == 0){
//                                ((PersonelTypeViewHolder) holder).time.setText("Just Now");
//                            } else {
//
//                                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
//                                String dateString = formatter.format(new Date(Long.parseLong(model.lastMessageTimeStamp)));
//                                ((PersonelTypeViewHolder) holder).time.setText(dateString);
//                                //((PersonelTypeViewHolder) holder).time.setText(String.valueOf(timestamp) + " minutes ago");
//                            }
//                        }

                        //((PersonelTypeViewHolder) holder).time.setText(String.valueOf(timestamp) + " minutes ago");

                        if (model.lastMessage.contains("https://") || model.lastMessage.contains("http://")){
                            model.lastMessage = "File";
                        }

                        ((PersonelTypeViewHolder) holder).message.setText(model.lastMessage);
                        break;
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                case InboxModel.GROUP_TYPE:
                    try {
                        Log.v("HereCalled__", "Herein adapter group 0");
                        ((GroupTypeViewHolder) holder).groupName.setText(model.name);

                        Glide.with(context).
                                load(model.img).
                                apply(new RequestOptions().placeholder(R.drawable.group).error(R.drawable.group)).
                                into(((GroupTypeViewHolder) holder).groupImage);

                        ((GroupTypeViewHolder) holder).parentBg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                context.startActivity(new Intent(context, GroupChatActivity.class).putExtra("grpName", model.name).putExtra("grpId", model.groupId));
                            }
                        });

                        if (!model.lastMessageSender.isEmpty()){
                            ((GroupTypeViewHolder) holder).lastMessageSender.setVisibility(View.VISIBLE);
                            ((GroupTypeViewHolder) holder).lastMessageSender.setText(model.lastMessageSender + " : ");
                        } else {
                            ((GroupTypeViewHolder) holder).lastMessageSender.setVisibility(View.GONE);
                        }

                        if (!model.lastMessage.isEmpty()){
                            ((GroupTypeViewHolder) holder).lastMessage.setVisibility(View.VISIBLE);

                            if (model.lastMessage.contains("https://") || model.lastMessage.contains("http://")){
                                model.lastMessage = "File";
                            }

                            ((GroupTypeViewHolder) holder).lastMessage.setText(model.lastMessage);
                        } else {
                            ((GroupTypeViewHolder) holder).lastMessage.setVisibility(View.GONE);
                        }

                        try {

                            long messageLongs = Long.parseLong(model.lastMessageTimeStamp);

                            Calendar nowCalender = Calendar.getInstance();
                            Calendar messageTimeCalender = Calendar.getInstance();
                            messageTimeCalender.setTimeInMillis(messageLongs);

                            if (DateUtils.isToday(messageLongs)) {
                                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
                                String dateString = formatter.format(new Date(Long.parseLong(model.lastMessageTimeStamp)));
                                ((GroupTypeViewHolder) holder).lastMessageTime.setText(dateString);
                            } else if (nowCalender.get(Calendar.DATE) == (messageTimeCalender.get(Calendar.DATE) + 1)) {
                                ((PersonelTypeViewHolder) holder).time.setText("Yesterday");
                            } else {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                String dateString = formatter.format(new Date(messageLongs));
                                ((GroupTypeViewHolder) holder).lastMessageTime.setText(dateString);
                            }

//                            double currentt = System.currentTimeMillis() - Double.parseDouble(model.lastMessageTimeStamp);
//
//                            double ttime = currentt / 1000;
//                            double minuttes  = ttime / 60;
//
//                            int ttimestamp = (int) minuttes;
//
//                            if (ttimestamp > 60){
//
//                                int hours = ttimestamp / 60;
//
//                                int minutess = ttimestamp % 60;
//
//                                if (hours > 24){
//
//                                    long curre = Long.parseLong(model.lastMessageTimeStamp);
//
//                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//                                    String dateString = formatter.format(new Date(curre));
//                                    ((GroupTypeViewHolder) holder).lastMessageTime.setText(dateString);
//
//                                } else {
//                                    ((GroupTypeViewHolder) holder).lastMessageTime.setText(String.valueOf(hours) + " h " + String.valueOf(minutess) + " min ago");
//                                }
//
//                            }else {
//
//                                if (ttimestamp == 0){
//                                    ((GroupTypeViewHolder) holder).lastMessageTime.setText("Just Now");
//                                } else {
//                                    ((GroupTypeViewHolder) holder).lastMessageTime.setText(String.valueOf(ttimestamp) + " minutes ago");
//                                }
//                            }
                        } catch (Exception e){
                            Log.v("Errorrr: " , e.getMessage());
                        }
                        break;
                    } catch (Exception e){
                        e.printStackTrace();
                    }
            }

        }
    }
}
