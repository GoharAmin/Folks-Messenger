package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Models.ChatModel;
import codegradients.com.chatapp.Models.ContactsForForwardScreenModel;
import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.ForwardMessageScreenAdapter;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class ForwardMessagesActivity extends AppCompatActivity {

    public static List<ContactsForForwardScreenModel> listForSelectedModels = new ArrayList<>();

    RecyclerView forwardContactsRecycler;
    ForwardMessageScreenAdapter adapter;
    List<ContactsForForwardScreenModel> allContacts = new ArrayList<>();
    ProgressBar progressBar;

    FloatingActionButton forwardIcon;

    String myName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_messages);

        myName = getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myName", "");

        initViews();
    }

    List<String> friendsIds = new ArrayList<>();

    private void initViews() {

        forwardIcon = findViewById(R.id.forwardIcon);
        forwardContactsRecycler = findViewById(R.id.forwardContactsRecycler);
        adapter = new ForwardMessageScreenAdapter(allContacts, ForwardMessagesActivity.this);
        forwardContactsRecycler.setAdapter(adapter);
        forwardContactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBarForwardScreen);
        progressBar.setVisibility(View.VISIBLE);
        getFriendsIds();
        
        forwardIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listForSelectedModels.size() == 0) {
                    Toast.makeText(ForwardMessagesActivity.this, "Select A Contact To Forward The Message", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //Toast.makeText(ForwardMessagesActivity.this, "Forwarding", Toast.LENGTH_SHORT).show();

                    for (ContactsForForwardScreenModel contactModel: listForSelectedModels) {

                        if (contactModel.isGroup()) {
                            //Is A group

                            for (ForwardModel forwardModel: HelperClass.forwardModelListToForwardMessages) {
                                String messageKey = String.valueOf(System.currentTimeMillis());
                                GroupMessageModel groupMessageModel = new GroupMessageModel(HelperClass.encrypt(forwardModel.getMessage()), contactModel.getSelectedId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), messageKey, HelperClass.encrypt(myName), messageKey, forwardModel.getMessageType());
                                FirebaseDatabase.getInstance().getReference().child("Groups").child(contactModel.getSelectedId()).child("Messages").child(messageKey).setValue(groupMessageModel);
                            }
                        } else {
                            //Is A Single Contact
                            for (ForwardModel forwardModel: HelperClass.forwardModelListToForwardMessages) {

                                String messageKey = String.valueOf(System.currentTimeMillis());

                                ChatModel modelToSend = new ChatModel(HelperClass.encrypt(forwardModel.getMessage()), HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(contactModel.getSelectedName()), contactModel.getSelectedId(), messageKey, messageKey, forwardModel.getMessageType(), false, false, "sent", "0", "0", false, "");
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(contactModel.getSelectedId()).child(messageKey).setValue(modelToSend);
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(contactModel.getSelectedId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(messageKey).setValue(modelToSend);
                                HelperClass.Send_NotificationForSingleMessage(forwardModel.getMessageType(), modelToSend, myName);
                            }
                        }
                    }

                    finish();
                }
            }
        });
    }

    private void getFriendsIds() {

        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("friends")) {

                            for (DataSnapshot dd: dataSnapshot.child("friends").getChildren()) {
                                friendsIds.add(dd.getKey());
                            }
                        } else {

                        }

                        if (friendsIds.size() == 0) {
                            getGroups();
                        } else {
                            getContacts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getContacts() {
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (String id: friendsIds) {

                    if (dataSnapshot.hasChild(id)) {

                        String name = HelperClass.decrypt(dataSnapshot.child(id).child("userName").getValue(String.class));
                        String image = HelperClass.decrypt(dataSnapshot.child(id).child("profileImage").getValue(String.class));

                        allContacts.add(new ContactsForForwardScreenModel(id, name, image, false));
                    }
                }

                getGroups();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroups() {

        FirebaseDatabase.getInstance().getReference().child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot d: dataSnapshot.getChildren()) {

                    if (d.child("users").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        String name = d.child("groupName").getValue(String.class);
                        String image = d.child("groupImage").getValue(String.class);

                        allContacts.add(new ContactsForForwardScreenModel(d.getKey(), name, image, true));
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}