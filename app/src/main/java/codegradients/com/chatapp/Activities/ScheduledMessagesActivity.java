package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Models.ScheduleMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.ScheduledMessageAdapter;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class ScheduledMessagesActivity extends AppCompatActivity {

    RecyclerView scheduledMessagesReycycler;
    List<ScheduleMessageModel> list = new ArrayList<>();
    ScheduledMessageAdapter adapter;

    AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_messages);

        initViews();
    }

    private void initViews(){
        findViewById(R.id.back_btn_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        scheduledMessagesReycycler = findViewById(R.id.scheduledMessagesRecycler);
        adapter = new ScheduledMessageAdapter(list, this);
        scheduledMessagesReycycler.setAdapter(adapter);
        scheduledMessagesReycycler.setLayoutManager(new LinearLayoutManager(this));

        avi = findViewById(R.id.avi);

        getScheduledMessages();
    }

    private void getScheduledMessages() {
        DatabaseReference mDatabaseForGettingSingleChatMessages = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForGettingSingleChatMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot dd: dataSnapshot.getChildren()){

                    for (DataSnapshot d: dd.getChildren()){
                        if (d.hasChild("timestamp")){
                            if (Long.parseLong(d.child("timestamp").getValue(String.class)) > System.currentTimeMillis()){
                                String message = HelperClass.decrypt(d.child("message").getValue(String.class));
                                String messageKey = d.child("messageKey").getValue(String.class);
                                String messageType = d.child("messageType").getValue(String.class);
                                String receiver_id = d.child("receiver_id").getValue(String.class);
                                String receiver_name = HelperClass.decrypt(d.child("receiver_name").getValue(String.class));
                                String timestamp = d.child("timestamp").getValue(String.class);
                                int receiverType = 0;

                                list.add(new ScheduleMessageModel(message, receiver_id, receiver_name, timestamp, messageKey, messageType, receiverType));
                            }
                        }
                    }
                }

                getMessagesFromGroupNow();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMessagesFromGroupNow(){
        DatabaseReference mDatabaseForGettingGroupMessages = FirebaseDatabase.getInstance().getReference().child("Groups");
        mDatabaseForGettingGroupMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dddd: dataSnapshot.getChildren()){
                    if (dddd.child("users").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {


                        for (DataSnapshot d: dddd.child("Messages").getChildren()){
                            String message = HelperClass.decrypt(d.child("message").getValue(String.class));
                            String messageKey = d.child("key").getValue(String.class);
                            String messageType = d.child("messageType").getValue(String.class);
                            String receiver_id = dddd.getKey();
                            String timestamp = d.child("timestamp").getValue(String.class);
                            String receiverName = dddd.child("groupName").getValue(String.class);
                            int receiverType = 1;

                            if (Long.parseLong(timestamp) > System.currentTimeMillis()){
                                list.add(new ScheduleMessageModel(message, receiver_id, receiverName, timestamp, messageKey, messageType, receiverType));
                            }
                        }
                    }
                }

                avi.setVisibility(View.GONE);

                if (list.size() == 0){
                    findViewById(R.id.noMessageText).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.noMessageText).setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}